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

  public SubmissionEntity() {}

  public SubmissionEntity(
    String id,
    String lectureId,
    String questionId,
    String studentId,
    Instant submittedAt,
    String answerText
  ) {
    this.id = id;
    this.lectureId = lectureId;
    this.questionId = questionId;
    this.studentId = studentId;
    this.submittedAt = submittedAt;
    this.answerText = answerText;
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
}
