package com.livequiz.backend.application.messaging;

import java.util.List;
import java.util.Optional;

public interface OutboxMessageRepository {
  void save(OutboxMessage outboxMessage);

  Optional<OutboxMessage> findById(String id);

  Optional<OutboxMessage> findUnpublishedByIdForUpdate(String id);

  List<OutboxMessage> findPending(int limit);
}
