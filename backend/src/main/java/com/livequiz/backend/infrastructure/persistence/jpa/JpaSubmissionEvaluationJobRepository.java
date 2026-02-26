package com.livequiz.backend.infrastructure.persistence.jpa;

import java.time.Instant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSubmissionEvaluationJobRepository
  extends JpaRepository<SubmissionEvaluationJobEntity, String> {
  @Modifying
  @Query(
    value =
    """
      update submission_evaluation_jobs
      set status = 'PROCESSING',
          attempt_count = attempt_count + 1,
          updated_at = :updatedAt,
          next_attempt_at = null,
          last_error = null
      where submission_id = :submissionId
        and status in ('QUEUED', 'RETRY_SCHEDULED')
    """,
    nativeQuery = true
  )
  int claimForProcessing(
    @Param("submissionId") String submissionId,
    @Param("updatedAt") Instant updatedAt
  );
}
