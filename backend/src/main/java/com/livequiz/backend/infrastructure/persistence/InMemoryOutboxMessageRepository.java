package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.application.messaging.OutboxMessage;
import com.livequiz.backend.application.messaging.OutboxMessageRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryOutboxMessageRepository implements OutboxMessageRepository {

  private final Map<String, OutboxMessage> messagesById = new ConcurrentHashMap<>();

  @Override
  public void save(OutboxMessage outboxMessage) {
    this.messagesById.put(outboxMessage.id(), outboxMessage);
  }

  @Override
  public Optional<OutboxMessage> findById(String id) {
    return Optional.ofNullable(this.messagesById.get(id));
  }

  @Override
  public Optional<OutboxMessage> findUnpublishedByIdForUpdate(String id) {
    OutboxMessage message = this.messagesById.get(id);
    if (message == null || message.publishedAt() != null) {
      return Optional.empty();
    }
    return Optional.of(message);
  }

  @Override
  public List<OutboxMessage> findPending(int limit) {
    return this.messagesById
      .values()
      .stream()
      .filter(message -> message.publishedAt() == null)
      .sorted(Comparator.comparing(OutboxMessage::createdAt))
      .limit(limit)
      .toList();
  }
}
