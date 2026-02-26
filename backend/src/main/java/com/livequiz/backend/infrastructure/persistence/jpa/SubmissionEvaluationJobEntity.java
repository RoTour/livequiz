package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "submission_evaluation_jobs")
public class SubmissionEvaluationJobEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String submissionId;

  @Column(length = 255, nullable = false)
  private String lectureId;

  @Column(length = 255, nullable = false)
  private String questionId;

  @Column(length = 255, nullable = false)
  private String studentId;

  @Column(length = 64, nullable = false)
  private String status;

  @Column(nullable = false)
  private int attemptCount;

  private Instant nextAttemptAt;

  @Column(columnDefinition = "text")
  private String lastError;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public SubmissionEvaluationJobEntity() {}

  public SubmissionEvaluationJobEntity(
    String submissionId,
    String lectureId,
    String questionId,
    String studentId,
    String status,
    int attemptCount,
    Instant nextAttemptAt,
    String lastError,
    Instant createdAt,
    Instant updatedAt
  ) {
    this.submissionId = submissionId;
    this.lectureId = lectureId;
    this.questionId = questionId;
    this.studentId = studentId;
    this.status = status;
    this.attemptCount = attemptCount;
    this.nextAttemptAt = nextAttemptAt;
    this.lastError = lastError;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getSubmissionId() {
    return submissionId;
  }

  public String getLectureId() {
    return lectureId;
  }

  public String getQuestionId() {
    return questionId;
  }

  public String getStudentId() {
    return studentId;
  }

  public String getStatus() {
    return status;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public String getLastError() {
    return lastError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
