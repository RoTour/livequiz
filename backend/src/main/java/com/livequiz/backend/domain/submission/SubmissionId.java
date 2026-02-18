package com.livequiz.backend.domain.submission;

public record SubmissionId(String value) {
  public SubmissionId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Submission ID cannot be empty");
    }
  }
}
