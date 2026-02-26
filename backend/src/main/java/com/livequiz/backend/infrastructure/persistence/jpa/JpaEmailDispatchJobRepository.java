package com.livequiz.backend.infrastructure.persistence.jpa;

import java.time.Instant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailDispatchJobRepository
  extends JpaRepository<EmailDispatchJobEntity, String> {
  @Modifying
  @Query(
    value =
    """
      update email_dispatch_jobs
      set status = 'PROCESSING',
          updated_at = :updatedAt,
          next_attempt_at = null,
          last_error = null
      where message_id = :messageId
        and status in ('QUEUED', 'RETRY_SCHEDULED')
    """,
    nativeQuery = true
  )
  int claimForProcessing(
    @Param("messageId") String messageId,
    @Param("updatedAt") Instant updatedAt
  );
}
