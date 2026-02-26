package com.livequiz.backend.infrastructure.messaging;

import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.OutboxMessage;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "livequiz.messaging.enabled", havingValue = "true")
public class OutboxPublisher {

  private final OutboxMessageRepository outboxMessageRepository;
  private final LiveQuizMessagingProperties messagingProperties;
  private final OutboxPublishWorker outboxPublishWorker;

  public OutboxPublisher(
    OutboxMessageRepository outboxMessageRepository,
    LiveQuizMessagingProperties messagingProperties,
    OutboxPublishWorker outboxPublishWorker
  ) {
    this.outboxMessageRepository = outboxMessageRepository;
    this.messagingProperties = messagingProperties;
    this.outboxPublishWorker = outboxPublishWorker;
  }

  @Scheduled(fixedDelayString = "${livequiz.messaging.outbox.poll-interval-ms:500}")
  public void publishPending() {
    List<OutboxMessage> pending = this.outboxMessageRepository.findPending(
        this.messagingProperties.outbox().batchSize()
      );
    for (OutboxMessage outboxMessage : pending) {
      this.outboxPublishWorker.publishSingle(outboxMessage.id());
    }
  }
}
