package com.livequiz.backend.application.messaging;

public enum SubmissionEvaluationJobStatus {
  QUEUED,
  PROCESSING,
  RETRY_SCHEDULED,
  FAILED_FINAL,
  COMPLETED,
}
