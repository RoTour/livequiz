package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.application.messaging.OutboxMessage;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresOutboxMessageRepository implements OutboxMessageRepository {

  private final JpaOutboxMessageRepository jpaOutboxMessageRepository;

  public JpaPostgresOutboxMessageRepository(JpaOutboxMessageRepository jpaOutboxMessageRepository) {
    this.jpaOutboxMessageRepository = jpaOutboxMessageRepository;
  }

  @Override
  public void save(OutboxMessage outboxMessage) {
    this.jpaOutboxMessageRepository.save(toEntity(outboxMessage));
  }

  @Override
  public Optional<OutboxMessage> findById(String id) {
    return this.jpaOutboxMessageRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<OutboxMessage> findUnpublishedByIdForUpdate(String id) {
    return this.jpaOutboxMessageRepository
      .findByIdAndPublishedAtIsNull(id)
      .map(this::toDomain);
  }

  @Override
  public List<OutboxMessage> findPending(int limit) {
    return this.jpaOutboxMessageRepository
      .findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, limit))
      .stream()
      .map(this::toDomain)
      .toList();
  }

  private OutboxMessageEntity toEntity(OutboxMessage outboxMessage) {
    return new OutboxMessageEntity(
      outboxMessage.id(),
      outboxMessage.eventType(),
      outboxMessage.routingKey(),
      outboxMessage.payloadJson(),
      outboxMessage.correlationId(),
      outboxMessage.createdAt(),
      outboxMessage.publishedAt(),
      outboxMessage.attemptCount(),
      outboxMessage.lastError()
    );
  }

  private OutboxMessage toDomain(OutboxMessageEntity entity) {
    return new OutboxMessage(
      entity.getId(),
      entity.getEventType(),
      entity.getRoutingKey(),
      entity.getPayloadJson(),
      entity.getCorrelationId(),
      entity.getCreatedAt(),
      entity.getPublishedAt(),
      entity.getAttemptCount(),
      entity.getLastError()
    );
  }
}
