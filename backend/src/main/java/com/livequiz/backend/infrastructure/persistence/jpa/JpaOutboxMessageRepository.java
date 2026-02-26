package com.livequiz.backend.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface JpaOutboxMessageRepository
  extends JpaRepository<OutboxMessageEntity, String> {
  List<OutboxMessageEntity> findByPublishedAtIsNullOrderByCreatedAtAsc(Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<OutboxMessageEntity> findByIdAndPublishedAtIsNull(String id);
}
