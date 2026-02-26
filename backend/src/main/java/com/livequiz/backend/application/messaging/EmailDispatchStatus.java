package com.livequiz.backend.application.messaging;

public enum EmailDispatchStatus {
  QUEUED,
  PROCESSING,
  SENT,
  RETRY_SCHEDULED,
  FAILED_FINAL,
}
