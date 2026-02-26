package com.livequiz.backend.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livequiz.backend.application.StudentVerificationEmailSender;
import com.livequiz.backend.application.messaging.EmailDailyQuotaUsageRepository;
import com.livequiz.backend.application.messaging.EmailDispatchJob;
import com.livequiz.backend.application.messaging.EmailDispatchJobRepository;
import com.livequiz.backend.application.messaging.EmailDispatchStatus;
import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "livequiz.messaging.enabled", havingValue = "true")
public class EmailDispatchConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailDispatchConsumer.class);

  private final ObjectMapper objectMapper;
  private final EmailDispatchJobRepository emailDispatchJobRepository;
  private final EmailDailyQuotaUsageRepository emailDailyQuotaUsageRepository;
  private final StudentVerificationEmailSender studentVerificationEmailSender;
  private final RabbitTemplate rabbitTemplate;
  private final LiveQuizMessagingProperties messagingProperties;
  private final int maxPerSecond;
  private final int maxPerDay;
  private final AtomicLong nextAllowedSendAtMillis = new AtomicLong(0);

  public EmailDispatchConsumer(
    ObjectMapper objectMapper,
    EmailDispatchJobRepository emailDispatchJobRepository,
    EmailDailyQuotaUsageRepository emailDailyQuotaUsageRepository,
    StudentVerificationEmailSender studentVerificationEmailSender,
    RabbitTemplate rabbitTemplate,
    LiveQuizMessagingProperties messagingProperties,
    @Value("${livequiz.email.ses.max-per-second:1}") int maxPerSecond,
    @Value("${livequiz.email.ses.max-per-day:200}") int maxPerDay
  ) {
    this.objectMapper = objectMapper;
    this.emailDispatchJobRepository = emailDispatchJobRepository;
    this.emailDailyQuotaUsageRepository = emailDailyQuotaUsageRepository;
    this.studentVerificationEmailSender = studentVerificationEmailSender;
    this.rabbitTemplate = rabbitTemplate;
    this.messagingProperties = messagingProperties;
    this.maxPerSecond = maxPerSecond <= 0 ? 1 : maxPerSecond;
    this.maxPerDay = maxPerDay <= 0 ? 200 : maxPerDay;
  }

  @RabbitListener(
    queues = "${livequiz.messaging.email.main-queue}",
    concurrency = "${livequiz.messaging.email.consumer-concurrency:1}"
  )
  @Transactional
  public void consume(String rawMessage, Message amqpMessage) {
    try {
      String messageId = readMessageId(rawMessage);
      if (messageId == null || messageId.isBlank()) {
        LOGGER.warn("Skipping email dispatch message without messageId");
        return;
      }

      EmailDispatchJob job = this.emailDispatchJobRepository
        .findByMessageId(messageId)
        .orElse(null);
      if (job == null) {
        LOGGER.warn("Skipping email dispatch for unknown messageId={}", messageId);
        return;
      }
      if (job.status() == EmailDispatchStatus.SENT || job.status() == EmailDispatchStatus.FAILED_FINAL) {
        return;
      }

      Instant now = Instant.now();
      boolean claimed = this.emailDispatchJobRepository.claimForProcessing(messageId, now);
      if (!claimed) {
        return;
      }
      job = this.emailDispatchJobRepository.findByMessageId(messageId).orElse(null);
      if (job == null) {
        LOGGER.warn("Skipping claimed email dispatch for unknown messageId={}", messageId);
        return;
      }

      int attempt = Math.max(readAttempt(amqpMessage), job.attemptCount());

      if (!reserveDailyQuota(now)) {
        Instant nextAttemptAt = nextUtcMidnight(now).plusSeconds(60);
        republishWithDelay(rawMessage, attempt + 1, Duration.between(now, nextAttemptAt));
        this.emailDispatchJobRepository.save(
            job.defer(now, nextAttemptAt, "SES_DAILY_QUOTA_EXHAUSTED")
          );
        return;
      }

      waitForRateLimit();

      this.studentVerificationEmailSender.sendVerificationEmail(
          job.toEmail(),
          job.verificationToken(),
          job.verificationUrl(),
          job.expiresAt()
        );
      this.emailDispatchJobRepository.save(job.markSent(now));
    } catch (Exception e) {
      handleProcessingFailure(rawMessage, amqpMessage, e);
    }
  }

  private void handleProcessingFailure(String rawMessage, Message amqpMessage, Exception error) {
    String messageId = readMessageId(rawMessage);
    if (messageId == null || messageId.isBlank()) {
      LOGGER.warn("Email dispatch failed without messageId", error);
      return;
    }

    EmailDispatchJob job = this.emailDispatchJobRepository.findByMessageId(messageId).orElse(null);
    if (job == null) {
      LOGGER.warn("Email dispatch failed for unknown messageId={}", messageId, error);
      return;
    }
    if (job.status() != EmailDispatchStatus.PROCESSING) {
      LOGGER.debug("Ignoring email dispatch failure for non-processing job messageId={}", messageId);
      return;
    }

    int attempt = Math.max(readAttempt(amqpMessage), job.attemptCount());
    boolean retryable = isRetryable(error);
    boolean canRetry = retryable && attempt < this.messagingProperties.email().maxAttempts();
    Instant now = Instant.now();

    if (canRetry) {
      Duration delay = RetryBackoffPolicy.emailDelayForAttempt(attempt);
      republishWithDelay(rawMessage, attempt + 1, delay);
      this.emailDispatchJobRepository.save(
          job.markRetryScheduled(
            now,
            now.plus(delay),
            error.getClass().getSimpleName() + ": " + error.getMessage()
          )
        );
      LOGGER.warn(
        "Email dispatch retry scheduled messageId={} attempt={} delay={}s",
        messageId,
        attempt,
        delay.toSeconds(),
        error
      );
      return;
    }

    this.rabbitTemplate.convertAndSend(
        this.messagingProperties.exchange(),
        this.messagingProperties.email().failedRoutingKey(),
        rawMessage,
        message -> {
          MessageProperties messageProperties = message.getMessageProperties();
          messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
          messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
          messageProperties.setMessageId(messageId);
          messageProperties.setHeader("x-attempt", attempt);
          return message;
        }
      );
    this.emailDispatchJobRepository.save(
        job.markFailedFinal(now, error.getClass().getSimpleName() + ": " + error.getMessage())
      );
    LOGGER.error("Email dispatch failed permanently messageId={} attempt={}", messageId, attempt, error);
  }

  private boolean reserveDailyQuota(Instant now) {
    LocalDate todayUtc = now.atZone(ZoneOffset.UTC).toLocalDate();
    return this.emailDailyQuotaUsageRepository.tryReserve(todayUtc, this.maxPerDay, now);
  }

  private void republishWithDelay(String rawMessage, int nextAttempt, Duration delay) {
    long delayMs = Math.max(1000L, delay.toMillis());
    String messageId = readMessageId(rawMessage);
    this.rabbitTemplate.convertAndSend(
        this.messagingProperties.exchange(),
        this.messagingProperties.email().retryRoutingKey(),
        rawMessage,
        message -> {
          MessageProperties messageProperties = message.getMessageProperties();
          messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
          messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
          messageProperties.setMessageId(messageId);
          messageProperties.setExpiration(String.valueOf(delayMs));
          messageProperties.setHeader("x-attempt", nextAttempt);
          return message;
        }
      );
  }

  private synchronized void waitForRateLimit() throws InterruptedException {
    long nowMillis = System.currentTimeMillis();
    long allowedAt = this.nextAllowedSendAtMillis.get();
    if (nowMillis < allowedAt) {
      Thread.sleep(allowedAt - nowMillis);
    }
    long intervalMillis = Math.max(1000L, 1000L / this.maxPerSecond);
    this.nextAllowedSendAtMillis.set(System.currentTimeMillis() + intervalMillis);
  }

  private int readAttempt(Message amqpMessage) {
    if (amqpMessage == null) {
      return 1;
    }
    Object value = amqpMessage.getMessageProperties().getHeaders().get("x-attempt");
    if (value instanceof Number number) {
      return Math.max(1, number.intValue());
    }
    if (value instanceof String text) {
      try {
        return Math.max(1, Integer.parseInt(text));
      } catch (NumberFormatException ignored) {}
    }
    return 1;
  }

  private String readMessageId(String rawMessage) {
    try {
      JsonNode root = this.objectMapper.readTree(rawMessage);
      JsonNode messageIdNode = root.get("messageId");
      return messageIdNode == null ? null : messageIdNode.asText(null);
    } catch (Exception ignored) {
      return null;
    }
  }

  private boolean isRetryable(Exception error) {
    return !(error instanceof IllegalArgumentException);
  }

  private Instant nextUtcMidnight(Instant now) {
    LocalDate tomorrow = now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1);
    return tomorrow.atStartOfDay().toInstant(ZoneOffset.UTC);
  }
}
