package com.livequiz.backend.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livequiz.backend.application.messaging.EmailDispatchJob;
import com.livequiz.backend.application.messaging.EmailDispatchJobRepository;
import com.livequiz.backend.application.messaging.EmailDispatchRequestedPayload;
import com.livequiz.backend.application.messaging.EmailDispatchStatus;
import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.MessageEnvelope;
import com.livequiz.backend.application.messaging.OutboxMessage;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import com.livequiz.backend.application.messaging.StudentVerificationEmailDispatchService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "livequiz.messaging.enabled", havingValue = "true")
public class RabbitStudentVerificationEmailDispatchService
  implements StudentVerificationEmailDispatchService {

  private final ObjectMapper objectMapper;
  private final OutboxMessageRepository outboxMessageRepository;
  private final EmailDispatchJobRepository emailDispatchJobRepository;
  private final LiveQuizMessagingProperties messagingProperties;

  public RabbitStudentVerificationEmailDispatchService(
    ObjectMapper objectMapper,
    OutboxMessageRepository outboxMessageRepository,
    EmailDispatchJobRepository emailDispatchJobRepository,
    LiveQuizMessagingProperties messagingProperties
  ) {
    this.objectMapper = objectMapper;
    this.outboxMessageRepository = outboxMessageRepository;
    this.emailDispatchJobRepository = emailDispatchJobRepository;
    this.messagingProperties = messagingProperties;
  }

  @Override
  public void dispatch(
    String email,
    String verificationToken,
    String verificationUrl,
    Instant expiresAt,
    String correlationId
  ) {
    Instant now = Instant.now();
    String messageId = UUID.randomUUID().toString();
    EmailDispatchRequestedPayload payload = new EmailDispatchRequestedPayload(
      messageId,
      email,
      verificationToken,
      verificationUrl,
      expiresAt.toString()
    );
    MessageEnvelope<EmailDispatchRequestedPayload> envelope = new MessageEnvelope<>(
      messageId,
      "EmailDispatchRequestedV1",
      "v1",
      correlationId,
      null,
      now.toString(),
      payload
    );
    String payloadJson = toJson(envelope);
    this.emailDispatchJobRepository.save(
        new EmailDispatchJob(
          messageId,
          email,
          verificationToken,
          verificationUrl,
          expiresAt,
          EmailDispatchStatus.QUEUED,
          0,
          null,
          null,
          now,
          now
        )
      );
    this.outboxMessageRepository.save(
        new OutboxMessage(
          messageId,
          "EmailDispatchRequestedV1",
          this.messagingProperties.email().requestedRoutingKey(),
          payloadJson,
          correlationId,
          now,
          null,
          0,
          null
        )
      );
  }

  private String toJson(Object value) {
    try {
      return this.objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize email dispatch envelope", e);
    }
  }
}
