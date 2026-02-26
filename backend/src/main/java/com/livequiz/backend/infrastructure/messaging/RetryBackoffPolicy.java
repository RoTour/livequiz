package com.livequiz.backend.infrastructure.messaging;

import java.time.Duration;

public final class RetryBackoffPolicy {

  private static final Duration[] EMAIL_DELAYS = new Duration[] {
    Duration.ofSeconds(30),
    Duration.ofMinutes(2),
    Duration.ofMinutes(10),
    Duration.ofMinutes(30),
    Duration.ofHours(2),
  };

  private static final Duration[] SUBMISSION_DELAYS = new Duration[] {
    Duration.ofSeconds(15),
    Duration.ofMinutes(1),
    Duration.ofMinutes(5),
    Duration.ofMinutes(15),
    Duration.ofHours(1),
  };

  private RetryBackoffPolicy() {}

  public static Duration emailDelayForAttempt(int attempt) {
    return delayForAttempt(EMAIL_DELAYS, attempt);
  }

  public static Duration submissionDelayForAttempt(int attempt) {
    return delayForAttempt(SUBMISSION_DELAYS, attempt);
  }

  private static Duration delayForAttempt(Duration[] delays, int attempt) {
    int index = Math.max(0, attempt - 1);
    if (index >= delays.length) {
      return delays[delays.length - 1];
    }
    return delays[index];
  }
}
