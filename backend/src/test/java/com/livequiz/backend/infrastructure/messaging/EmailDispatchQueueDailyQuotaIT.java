package com.livequiz.backend.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.livequiz.backend.application.messaging.EmailDispatchJob;
import com.livequiz.backend.application.messaging.EmailDispatchJobRepository;
import com.livequiz.backend.application.messaging.EmailDispatchStatus;
import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.StudentVerificationEmailDispatchService;
import com.livequiz.backend.infrastructure.messaging.EmailDispatchQueueTestConfig.CapturedRabbitMessage;
import com.livequiz.backend.infrastructure.messaging.EmailDispatchQueueTestConfig.FakeRabbitTemplate;
import com.livequiz.backend.infrastructure.messaging.EmailDispatchQueueTestConfig.FakeSesStudentVerificationEmailSender;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
  properties = {
    "livequiz.messaging.enabled=true",
    "spring.rabbitmq.listener.simple.auto-startup=false",
    "spring.rabbitmq.listener.direct.auto-startup=false",
    "livequiz.email.ses.max-per-second=10",
    "livequiz.email.ses.max-per-day=1",
  }
)
@ActiveProfiles("in-memory")
@Import(EmailDispatchQueueTestConfig.class)
class EmailDispatchQueueDailyQuotaIT {

  @Autowired
  private StudentVerificationEmailDispatchService emailDispatchService;

  @Autowired
  private OutboxPublisher outboxPublisher;

  @Autowired
  private EmailDispatchConsumer emailDispatchConsumer;

  @Autowired
  private FakeRabbitTemplate fakeRabbitTemplate;

  @Autowired
  private FakeSesStudentVerificationEmailSender fakeSesStudentVerificationEmailSender;

  @Autowired
  private EmailDispatchJobRepository emailDispatchJobRepository;

  @Autowired
  private LiveQuizMessagingProperties messagingProperties;

  @BeforeEach
  void setUp() {
    this.fakeRabbitTemplate.clear();
    this.fakeSesStudentVerificationEmailSender.clear();
  }

  @Test
  void should_defer_second_job_when_daily_ses_sandbox_quota_is_exhausted() {
    dispatchVerificationEmail("quota1@ynov.com", "quota-correlation-1");
    dispatchVerificationEmail("quota2@ynov.com", "quota-correlation-2");

    this.outboxPublisher.publishPending();

    List<CapturedRabbitMessage> requestedMessages = this.fakeRabbitTemplate.messagesForRoutingKey(
        this.messagingProperties.email().requestedRoutingKey()
      );
    assertEquals(2, requestedMessages.size());

    for (CapturedRabbitMessage requestedMessage : requestedMessages) {
      this.emailDispatchConsumer.consume(requestedMessage.payloadAsString(), requestedMessage.message());
    }

    assertEquals(1, this.fakeSesStudentVerificationEmailSender.sentEmails().size());

    List<EmailDispatchJob> jobs = requestedMessages
      .stream()
      .map(capturedMessage -> capturedMessage.message().getMessageProperties().getMessageId())
      .map(messageId -> this.emailDispatchJobRepository.findByMessageId(messageId).orElseThrow())
      .toList();

    java.util.Map<EmailDispatchStatus, Long> statusCounts = jobs
      .stream()
      .collect(Collectors.groupingBy(EmailDispatchJob::status, Collectors.counting()));
    assertEquals(1L, statusCounts.getOrDefault(EmailDispatchStatus.SENT, 0L));
    assertEquals(1L, statusCounts.getOrDefault(EmailDispatchStatus.RETRY_SCHEDULED, 0L));

    EmailDispatchJob deferredJob = jobs
      .stream()
      .filter(job -> job.status() == EmailDispatchStatus.RETRY_SCHEDULED)
      .findFirst()
      .orElseThrow();
    assertEquals(0, deferredJob.attemptCount());
    assertEquals("SES_DAILY_QUOTA_EXHAUSTED", deferredJob.lastError());
    assertNotNull(deferredJob.nextAttemptAt());

    List<CapturedRabbitMessage> retryMessages = this.fakeRabbitTemplate.messagesForRoutingKey(
        this.messagingProperties.email().retryRoutingKey()
      );
    assertEquals(1, retryMessages.size());

    Message retryMessage = retryMessages.get(0).message();
    Object attemptHeader = retryMessage.getMessageProperties().getHeaders().get("x-attempt");
    assertEquals(2, attemptHeader);

    String expiration = retryMessage.getMessageProperties().getExpiration();
    assertNotNull(expiration);
  }

  private void dispatchVerificationEmail(String email, String correlationId) {
    this.emailDispatchService.dispatch(
        email,
        "verification-token-" + correlationId,
        "http://localhost:4200/student/verify-email?token=" + correlationId,
        Instant.now().plusSeconds(1800),
        correlationId
      );
  }
}
