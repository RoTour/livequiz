package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "submission_attempts")
public class SubmissionEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String id;

  @Column(length = 255, nullable = false)
  private String lectureId;

  @Column(length = 255, nullable = false)
  private String questionId;

  @Column(length = 255, nullable = false)
  private String studentId;

  @Column(nullable = false)
  private Instant submittedAt;

  @Column(columnDefinition = "text", nullable = false)
  private String answerText;

  @Column(length = 64, nullable = false)
  private String answerStatus;

  private Instant evaluationCompletedAt;

  @Column(nullable = false)
  private boolean reviewPublished;

  private Instant reviewCreatedAt;

  private Instant reviewPublishedAt;

  @Column(length = 255)
  private String reviewedByInstructorId;

  @Column(length = 64)
  private String reviewOrigin;

  private Boolean feedbackIsCorrect;

  @Column(columnDefinition = "text")
  private String feedbackComment;

  @Column(columnDefinition = "text")
  private String feedbackMissingKeyPoints;

  @Column(length = 64)
  private String llmSuggestedStatus;

  @Column(columnDefinition = "text")
  private String llmSuggestedComment;

  @Column(columnDefinition = "text")
  private String llmSuggestedMissingKeyPoints;

  private Instant llmSuggestedAt;

  @Column(length = 255)
  private String llmSuggestedModel;

  private Instant llmAcceptedAt;

  @Column(length = 255)
  private String llmAcceptedByInstructorId;

  public SubmissionEntity() {}

  public SubmissionEntity(
    String id,
    String lectureId,
    String questionId,
    String studentId,
    Instant submittedAt,
    String answerText,
    String answerStatus,
    Instant evaluationCompletedAt,
    boolean reviewPublished,
    Instant reviewCreatedAt,
    Instant reviewPublishedAt,
    String reviewedByInstructorId,
    String reviewOrigin,
    Boolean feedbackIsCorrect,
    String feedbackComment,
    String feedbackMissingKeyPoints,
    String llmSuggestedStatus,
    String llmSuggestedComment,
    String llmSuggestedMissingKeyPoints,
    Instant llmSuggestedAt,
    String llmSuggestedModel,
    Instant llmAcceptedAt,
    String llmAcceptedByInstructorId
  ) {
    this.id = id;
    this.lectureId = lectureId;
    this.questionId = questionId;
    this.studentId = studentId;
    this.submittedAt = submittedAt;
    this.answerText = answerText;
    this.answerStatus = answerStatus;
    this.evaluationCompletedAt = evaluationCompletedAt;
    this.reviewPublished = reviewPublished;
    this.reviewCreatedAt = reviewCreatedAt;
    this.reviewPublishedAt = reviewPublishedAt;
    this.reviewedByInstructorId = reviewedByInstructorId;
    this.reviewOrigin = reviewOrigin;
    this.feedbackIsCorrect = feedbackIsCorrect;
    this.feedbackComment = feedbackComment;
    this.feedbackMissingKeyPoints = feedbackMissingKeyPoints;
    this.llmSuggestedStatus = llmSuggestedStatus;
    this.llmSuggestedComment = llmSuggestedComment;
    this.llmSuggestedMissingKeyPoints = llmSuggestedMissingKeyPoints;
    this.llmSuggestedAt = llmSuggestedAt;
    this.llmSuggestedModel = llmSuggestedModel;
    this.llmAcceptedAt = llmAcceptedAt;
    this.llmAcceptedByInstructorId = llmAcceptedByInstructorId;
  }

  public String getId() {
    return id;
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

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public String getAnswerText() {
    return answerText;
  }

  public String getAnswerStatus() {
    return answerStatus;
  }

  public Instant getEvaluationCompletedAt() {
    return evaluationCompletedAt;
  }

  public boolean isReviewPublished() {
    return reviewPublished;
  }

  public Instant getReviewCreatedAt() {
    return reviewCreatedAt;
  }

  public Instant getReviewPublishedAt() {
    return reviewPublishedAt;
  }

  public String getReviewedByInstructorId() {
    return reviewedByInstructorId;
  }

  public String getReviewOrigin() {
    return reviewOrigin;
  }

  public Boolean getFeedbackIsCorrect() {
    return feedbackIsCorrect;
  }

  public String getFeedbackComment() {
    return feedbackComment;
  }

  public String getFeedbackMissingKeyPoints() {
    return feedbackMissingKeyPoints;
  }

  public String getLlmSuggestedStatus() {
    return llmSuggestedStatus;
  }

  public String getLlmSuggestedComment() {
    return llmSuggestedComment;
  }

  public String getLlmSuggestedMissingKeyPoints() {
    return llmSuggestedMissingKeyPoints;
  }

  public Instant getLlmSuggestedAt() {
    return llmSuggestedAt;
  }

  public String getLlmSuggestedModel() {
    return llmSuggestedModel;
  }

  public Instant getLlmAcceptedAt() {
    return llmAcceptedAt;
  }

  public String getLlmAcceptedByInstructorId() {
    return llmAcceptedByInstructorId;
  }
}
