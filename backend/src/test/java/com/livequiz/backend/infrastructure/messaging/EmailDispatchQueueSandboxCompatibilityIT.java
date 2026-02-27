package com.livequiz.backend.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.livequiz.backend.application.messaging.EmailDispatchJobRepository;
import com.livequiz.backend.application.messaging.EmailDispatchStatus;
import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import com.livequiz.backend.application.messaging.StudentVerificationEmailDispatchService;
import com.livequiz.backend.infrastructure.messaging.EmailDispatchQueueTestConfig.CapturedRabbitMessage;
import com.livequiz.backend.infrastructure.messaging.EmailDispatchQueueTestConfig.FakeRabbitTemplate;
import com.livequiz.backend.infrastructure.messaging.EmailDispatchQueueTestConfig.FakeSesStudentVerificationEmailSender;
import java.time.Instant;
import java.util.List;
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
    "livequiz.email.ses.max-per-second=1",
    "livequiz.email.ses.max-per-day=200",
  }
)
@ActiveProfiles("in-memory")
@Import(EmailDispatchQueueTestConfig.class)
class EmailDispatchQueueSandboxCompatibilityIT {

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
  private OutboxMessageRepository outboxMessageRepository;

  @Autowired
  private LiveQuizMessagingProperties messagingProperties;

  @BeforeEach
  void setUp() {
    this.fakeRabbitTemplate.clear();
    this.fakeSesStudentVerificationEmailSender.clear();
  }

  @Test
  void should_dispatch_and_consume_email_jobs_with_ses_sandbox_rate_limit() {
    dispatchVerificationEmail("student1@ynov.com", "correlation-1");
    dispatchVerificationEmail("student2@ynov.com", "correlation-2");

    this.outboxPublisher.publishPending();

    List<CapturedRabbitMessage> requestedMessages = this.fakeRabbitTemplate.messagesForRoutingKey(
        this.messagingProperties.email().requestedRoutingKey()
      );
    assertEquals(2, requestedMessages.size());

    for (CapturedRabbitMessage requestedMessage : requestedMessages) {
      Message amqpMessage = requestedMessage.message();
      this.emailDispatchConsumer.consume(requestedMessage.payloadAsString(), amqpMessage);
      String messageId = amqpMessage.getMessageProperties().getMessageId();
      EmailDispatchStatus status = this.emailDispatchJobRepository
        .findByMessageId(messageId)
        .orElseThrow()
        .status();
      assertEquals(EmailDispatchStatus.SENT, status);
    }

    assertEquals(2, this.fakeSesStudentVerificationEmailSender.sentEmails().size());

    List<CapturedRabbitMessage> retryMessages = this.fakeRabbitTemplate.messagesForRoutingKey(
        this.messagingProperties.email().retryRoutingKey()
      );
    assertEquals(0, retryMessages.size());

    assertTrue(this.outboxMessageRepository.findPending(10).isEmpty());
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
