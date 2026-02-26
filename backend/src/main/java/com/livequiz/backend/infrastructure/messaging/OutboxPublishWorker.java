package com.livequiz.backend.infrastructure.messaging;

import com.livequiz.backend.application.messaging.LiveQuizMessagingProperties;
import com.livequiz.backend.application.messaging.OutboxMessage;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublishWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxPublishWorker.class);

  private final OutboxMessageRepository outboxMessageRepository;
  private final RabbitTemplate rabbitTemplate;
  private final LiveQuizMessagingProperties messagingProperties;

  public OutboxPublishWorker(
    OutboxMessageRepository outboxMessageRepository,
    RabbitTemplate rabbitTemplate,
    LiveQuizMessagingProperties messagingProperties
  ) {
    this.outboxMessageRepository = outboxMessageRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.messagingProperties = messagingProperties;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void publishSingle(String outboxId) {
    OutboxMessage outboxMessage = this.outboxMessageRepository
      .findUnpublishedByIdForUpdate(outboxId)
      .orElse(null);
    if (outboxMessage == null) {
      return;
    }
    try {
      this.rabbitTemplate.convertAndSend(
          this.messagingProperties.exchange(),
          outboxMessage.routingKey(),
          outboxMessage.payloadJson(),
          message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            messageProperties.setMessageId(outboxMessage.id());
            messageProperties.setHeader("x-attempt", outboxMessage.attemptCount() + 1);
            return message;
          }
        );
      this.outboxMessageRepository.save(outboxMessage.markPublished(Instant.now()));
    } catch (Exception e) {
      LOGGER.warn(
        "Failed to publish outbox message id={} eventType={}",
        outboxMessage.id(),
        outboxMessage.eventType(),
        e
      );
      this.outboxMessageRepository.save(
          outboxMessage.markPublishFailed(
            e.getClass().getSimpleName() + ": " + e.getMessage(),
            Instant.now()
          )
        );
    }
  }
}
