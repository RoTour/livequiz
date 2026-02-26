package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.application.messaging.SubmissionEvaluationJob;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobRepository;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemorySubmissionEvaluationJobRepository
  implements SubmissionEvaluationJobRepository {

  private final Map<String, SubmissionEvaluationJob> jobsBySubmissionId =
    new ConcurrentHashMap<>();

  @Override
  public void save(SubmissionEvaluationJob submissionEvaluationJob) {
    this.jobsBySubmissionId.put(
        submissionEvaluationJob.submissionId(),
        submissionEvaluationJob
      );
  }

  @Override
  public Optional<SubmissionEvaluationJob> findBySubmissionId(String submissionId) {
    return Optional.ofNullable(this.jobsBySubmissionId.get(submissionId));
  }

  @Override
  public synchronized boolean claimForProcessing(String submissionId, Instant now) {
    SubmissionEvaluationJob existing = this.jobsBySubmissionId.get(submissionId);
    if (existing == null) {
      return false;
    }
    if (
      existing.status() != SubmissionEvaluationJobStatus.QUEUED &&
      existing.status() != SubmissionEvaluationJobStatus.RETRY_SCHEDULED
    ) {
      return false;
    }
    this.jobsBySubmissionId.put(submissionId, existing.markProcessing(now));
    return true;
  }
}
