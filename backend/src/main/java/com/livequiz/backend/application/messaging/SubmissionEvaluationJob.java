package com.livequiz.backend.application.messaging;

import java.time.Instant;

public record SubmissionEvaluationJob(
  String submissionId,
  String lectureId,
  String questionId,
  String studentId,
  SubmissionEvaluationJobStatus status,
  int attemptCount,
  Instant nextAttemptAt,
  String lastError,
  Instant createdAt,
  Instant updatedAt
) {
  public SubmissionEvaluationJob {
    if (submissionId == null || submissionId.isBlank()) {
      throw new IllegalArgumentException("Submission evaluation job ID cannot be blank");
    }
    if (lectureId == null || lectureId.isBlank()) {
      throw new IllegalArgumentException("Submission evaluation lecture ID cannot be blank");
    }
    if (questionId == null || questionId.isBlank()) {
      throw new IllegalArgumentException("Submission evaluation question ID cannot be blank");
    }
    if (studentId == null || studentId.isBlank()) {
      throw new IllegalArgumentException("Submission evaluation student ID cannot be blank");
    }
    if (status == null) {
      throw new IllegalArgumentException("Submission evaluation status cannot be null");
    }
    if (attemptCount < 0) {
      throw new IllegalArgumentException("Submission evaluation attempts cannot be negative");
    }
    if (createdAt == null || updatedAt == null) {
      throw new IllegalArgumentException("Submission evaluation timestamps cannot be null");
    }
  }

  public SubmissionEvaluationJob markProcessing(Instant now) {
    return new SubmissionEvaluationJob(
      this.submissionId,
      this.lectureId,
      this.questionId,
      this.studentId,
      SubmissionEvaluationJobStatus.PROCESSING,
      this.attemptCount + 1,
      null,
      null,
      this.createdAt,
      now
    );
  }

  public SubmissionEvaluationJob markRetryScheduled(
    Instant now,
    Instant nextAttemptAt,
    String error
  ) {
    return new SubmissionEvaluationJob(
      this.submissionId,
      this.lectureId,
      this.questionId,
      this.studentId,
      SubmissionEvaluationJobStatus.RETRY_SCHEDULED,
      this.attemptCount,
      nextAttemptAt,
      error,
      this.createdAt,
      now
    );
  }

  public SubmissionEvaluationJob markCompleted(Instant now) {
    return new SubmissionEvaluationJob(
      this.submissionId,
      this.lectureId,
      this.questionId,
      this.studentId,
      SubmissionEvaluationJobStatus.COMPLETED,
      this.attemptCount,
      null,
      null,
      this.createdAt,
      now
    );
  }

  public SubmissionEvaluationJob markFailedFinal(Instant now, String error) {
    return new SubmissionEvaluationJob(
      this.submissionId,
      this.lectureId,
      this.questionId,
      this.studentId,
      SubmissionEvaluationJobStatus.FAILED_FINAL,
      this.attemptCount,
      null,
      error,
      this.createdAt,
      now
    );
  }
}
