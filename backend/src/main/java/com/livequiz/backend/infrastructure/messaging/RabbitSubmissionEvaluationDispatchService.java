package com.livequiz.backend.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.MessageEnvelope;
import com.livequiz.backend.application.messaging.OutboxMessage;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import com.livequiz.backend.application.messaging.SubmissionEvaluationDispatchService;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJob;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobRepository;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobStatus;
import com.livequiz.backend.application.messaging.SubmissionEvaluationRequestedPayload;
import com.livequiz.backend.domain.submission.Submission;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "livequiz.messaging.enabled", havingValue = "true")
public class RabbitSubmissionEvaluationDispatchService
  implements SubmissionEvaluationDispatchService {

  private final ObjectMapper objectMapper;
  private final OutboxMessageRepository outboxMessageRepository;
  private final SubmissionEvaluationJobRepository submissionEvaluationJobRepository;
  private final LiveQuizMessagingProperties messagingProperties;

  public RabbitSubmissionEvaluationDispatchService(
    ObjectMapper objectMapper,
    OutboxMessageRepository outboxMessageRepository,
    SubmissionEvaluationJobRepository submissionEvaluationJobRepository,
    LiveQuizMessagingProperties messagingProperties
  ) {
    this.objectMapper = objectMapper;
    this.outboxMessageRepository = outboxMessageRepository;
    this.submissionEvaluationJobRepository = submissionEvaluationJobRepository;
    this.messagingProperties = messagingProperties;
  }

  @Override
  public void dispatch(Submission submission, String correlationId) {
    Instant now = Instant.now();
    String messageId = UUID.randomUUID().toString();
    SubmissionEvaluationRequestedPayload payload = new SubmissionEvaluationRequestedPayload(
      messageId,
      submission.id().value(),
      submission.lectureId().value(),
      submission.questionId().value(),
      submission.studentId()
    );
    MessageEnvelope<SubmissionEvaluationRequestedPayload> envelope = new MessageEnvelope<>(
      messageId,
      "SubmissionEvaluationRequestedV1",
      "v1",
      correlationId,
      submission.id().value(),
      now.toString(),
      payload
    );
    String payloadJson = toJson(envelope);

    this.submissionEvaluationJobRepository.save(
        new SubmissionEvaluationJob(
          submission.id().value(),
          submission.lectureId().value(),
          submission.questionId().value(),
          submission.studentId(),
          SubmissionEvaluationJobStatus.QUEUED,
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
          "SubmissionEvaluationRequestedV1",
          this.messagingProperties.submission().requestedRoutingKey(),
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
      throw new IllegalStateException("Failed to serialize submission evaluation envelope", e);
    }
  }
}
