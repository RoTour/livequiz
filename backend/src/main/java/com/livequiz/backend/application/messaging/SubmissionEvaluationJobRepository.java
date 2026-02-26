package com.livequiz.backend.application.messaging;

import java.util.Optional;
import java.time.Instant;

public interface SubmissionEvaluationJobRepository {
  void save(SubmissionEvaluationJob submissionEvaluationJob);

  Optional<SubmissionEvaluationJob> findBySubmissionId(String submissionId);

  boolean claimForProcessing(String submissionId, Instant now);
}
