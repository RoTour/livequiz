package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "submission_attempts")
public class SubmissionEntity {

  @Id
  private String id;

  private String lectureId;
  private String questionId;
  private String studentId;
  private Instant submittedAt;
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
