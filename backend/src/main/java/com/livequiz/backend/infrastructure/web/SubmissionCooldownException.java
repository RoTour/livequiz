package com.livequiz.backend.infrastructure.web;

import org.springframework.http.HttpStatus;

public class SubmissionCooldownException extends ApiException {

  private final long retryAfterSeconds;

  public SubmissionCooldownException(long retryAfterSeconds) {
    super(
      HttpStatus.TOO_MANY_REQUESTS,
      "SUBMISSION_COOLDOWN",
      "Please wait " + retryAfterSeconds + "s before submitting again"
    );
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public long retryAfterSeconds() {
    return retryAfterSeconds;
  }
}
