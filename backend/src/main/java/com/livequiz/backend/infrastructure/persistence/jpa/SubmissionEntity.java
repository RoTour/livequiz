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

  private Boolean feedbackIsCorrect;

  @Column(columnDefinition = "text")
  private String feedbackComment;

  @Column(columnDefinition = "text")
  private String feedbackMissingKeyPoints;

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
    Boolean feedbackIsCorrect,
    String feedbackComment,
    String feedbackMissingKeyPoints
  ) {
    this.id = id;
    this.lectureId = lectureId;
    this.questionId = questionId;
    this.studentId = studentId;
    this.submittedAt = submittedAt;
    this.answerText = answerText;
    this.answerStatus = answerStatus;
    this.evaluationCompletedAt = evaluationCompletedAt;
    this.feedbackIsCorrect = feedbackIsCorrect;
    this.feedbackComment = feedbackComment;
    this.feedbackMissingKeyPoints = feedbackMissingKeyPoints;
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

  public Boolean getFeedbackIsCorrect() {
    return feedbackIsCorrect;
  }

  public String getFeedbackComment() {
    return feedbackComment;
  }

  public String getFeedbackMissingKeyPoints() {
    return feedbackMissingKeyPoints;
  }
}
