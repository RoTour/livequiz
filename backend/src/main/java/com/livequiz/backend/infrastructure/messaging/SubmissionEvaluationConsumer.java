package com.livequiz.backend.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livequiz.backend.application.AnswerEvaluationProvider;
import com.livequiz.backend.application.AnswerEvaluationStatus;
import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJob;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobRepository;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobStatus;
import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "livequiz.messaging.enabled", havingValue = "true")
public class SubmissionEvaluationConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    SubmissionEvaluationConsumer.class
  );

  private final ObjectMapper objectMapper;
  private final SubmissionEvaluationJobRepository submissionEvaluationJobRepository;
  private final SubmissionRepository submissionRepository;
  private final LectureRepository lectureRepository;
  private final AnswerEvaluationProvider answerEvaluationProvider;
  private final RabbitTemplate rabbitTemplate;
  private final LiveQuizMessagingProperties messagingProperties;

  public SubmissionEvaluationConsumer(
    ObjectMapper objectMapper,
    SubmissionEvaluationJobRepository submissionEvaluationJobRepository,
    SubmissionRepository submissionRepository,
    LectureRepository lectureRepository,
    AnswerEvaluationProvider answerEvaluationProvider,
    RabbitTemplate rabbitTemplate,
    LiveQuizMessagingProperties messagingProperties
  ) {
    this.objectMapper = objectMapper;
    this.submissionEvaluationJobRepository = submissionEvaluationJobRepository;
    this.submissionRepository = submissionRepository;
    this.lectureRepository = lectureRepository;
    this.answerEvaluationProvider = answerEvaluationProvider;
    this.rabbitTemplate = rabbitTemplate;
    this.messagingProperties = messagingProperties;
  }

  @RabbitListener(
    queues = "${livequiz.messaging.submission.main-queue}",
    concurrency = "${livequiz.messaging.submission.consumer-concurrency:2}"
  )
  @Transactional
  public void consume(String rawMessage, Message amqpMessage) {
    SubmissionEvaluationJob processingJob = null;
    try {
      PayloadIdentifiers identifiers = readIdentifiers(rawMessage);
      if (identifiers == null) {
        LOGGER.warn("Skipping submission evaluation message with invalid payload");
        return;
      }

      Instant now = Instant.now();
      boolean claimed = this.submissionEvaluationJobRepository.claimForProcessing(
        identifiers.submissionId(),
        now
      );
      if (!claimed) {
        return;
      }

      SubmissionEvaluationJob job = this.submissionEvaluationJobRepository
        .findBySubmissionId(identifiers.submissionId())
        .orElse(null);
      if (job == null) {
        LOGGER.warn(
          "Skipping submission evaluation for unknown submissionId={}",
          identifiers.submissionId()
        );
        return;
      }

      if (job.status() != SubmissionEvaluationJobStatus.PROCESSING) {
        return;
      }
      processingJob = job;

      Submission submission = this.submissionRepository
        .findById(new SubmissionId(identifiers.submissionId()))
        .orElseThrow(() ->
          new IllegalStateException(
            "Submission not found for evaluation: " + identifiers.submissionId()
          )
        );

      Lecture lecture = this.lectureRepository
        .findById(new LectureId(submission.lectureId().value()))
        .orElseThrow(() ->
          new IllegalStateException(
            "Lecture not found for submission evaluation: " + submission.lectureId().value()
          )
        );

      com.livequiz.backend.domain.lecture.Question question = lecture
        .questions()
        .stream()
        .filter(existingQuestion ->
          existingQuestion.id().value().equals(submission.questionId().value())
        )
        .findFirst()
        .orElseThrow(() ->
          new IllegalStateException(
            "Question not found for submission evaluation: " + submission.questionId().value()
          )
        );

      AnswerEvaluationProvider.EvaluationResult evaluationResult = this.answerEvaluationProvider.evaluate(
          question.prompt(),
          question.modelAnswer(),
          submission.answerText()
        );

      submission.recordLlmSuggestion(
        evaluationResult.status().name(),
        evaluationResult.feedback().missingKeyPoints(),
        evaluationResult.feedback().comment(),
        evaluationResult.model(),
        Instant.now()
      );
      this.submissionRepository.save(submission);
      this.submissionEvaluationJobRepository.save(processingJob.markCompleted(Instant.now()));
    } catch (Exception e) {
      handleProcessingFailure(rawMessage, amqpMessage, e, processingJob);
    }
  }

  private void handleProcessingFailure(
    String rawMessage,
    Message amqpMessage,
    Exception error,
    SubmissionEvaluationJob processingJob
  ) {
    PayloadIdentifiers identifiers = readIdentifiers(rawMessage);
    if (identifiers == null) {
      LOGGER.warn("Submission evaluation failed with invalid payload", error);
      return;
    }

    SubmissionEvaluationJob job = processingJob;
    if (job == null) {
      job = this.submissionEvaluationJobRepository
        .findBySubmissionId(identifiers.submissionId())
        .orElse(null);
    }
    if (job == null) {
      LOGGER.warn(
        "Submission evaluation failed for unknown submissionId={}",
        identifiers.submissionId(),
        error
      );
      return;
    }
    if (job.status() != SubmissionEvaluationJobStatus.PROCESSING) {
      return;
    }

    int attempt = Math.max(readAttempt(amqpMessage), job.attemptCount());
    boolean retryable = isRetryable(error);
    boolean canRetry = retryable && attempt < this.messagingProperties.submission().maxAttempts();
    Instant now = Instant.now();

    if (canRetry) {
      Duration delay = RetryBackoffPolicy.submissionDelayForAttempt(attempt);
      republishWithDelay(rawMessage, identifiers.messageId(), attempt + 1, delay);
      this.submissionEvaluationJobRepository.save(
          job.markRetryScheduled(
            now,
            now.plus(delay),
            error.getClass().getSimpleName() + ": " + error.getMessage()
          )
        );
      LOGGER.warn(
        "Submission evaluation retry scheduled submissionId={} attempt={} delay={}s",
        identifiers.submissionId(),
        attempt,
        delay.toSeconds(),
        error
      );
      return;
    }

    this.rabbitTemplate.convertAndSend(
        this.messagingProperties.exchange(),
        this.messagingProperties.submission().failedRoutingKey(),
        rawMessage,
        message -> {
          MessageProperties messageProperties = message.getMessageProperties();
          messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
          messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
          messageProperties.setMessageId(identifiers.messageId());
          messageProperties.setHeader("x-attempt", attempt);
          return message;
        }
      );
    this.submissionEvaluationJobRepository.save(
        job.markFailedFinal(now, error.getClass().getSimpleName() + ": " + error.getMessage())
      );
    LOGGER.error(
      "Submission evaluation failed permanently submissionId={} attempt={}",
      identifiers.submissionId(),
      attempt,
      error
    );
  }

  private void republishWithDelay(
    String rawMessage,
    String messageId,
    int nextAttempt,
    Duration delay
  ) {
    long delayMs = Math.max(1000L, delay.toMillis());
    this.rabbitTemplate.convertAndSend(
        this.messagingProperties.exchange(),
        this.messagingProperties.submission().retryRoutingKey(),
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

  private boolean isRetryable(Exception error) {
    return !(error instanceof IllegalArgumentException);
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

  private PayloadIdentifiers readIdentifiers(String rawMessage) {
    try {
      JsonNode root = this.objectMapper.readTree(rawMessage);
      String messageId = asText(root.get("messageId"));
      JsonNode payload = root.get("payload");
      if (payload == null) {
        return null;
      }
      String submissionId = asText(payload.get("submissionId"));
      if (messageId == null || submissionId == null) {
        return null;
      }
      return new PayloadIdentifiers(messageId, submissionId);
    } catch (Exception e) {
      return null;
    }
  }

  private String asText(JsonNode node) {
    return node == null ? null : node.asText(null);
  }

  private record PayloadIdentifiers(String messageId, String submissionId) {}
}
