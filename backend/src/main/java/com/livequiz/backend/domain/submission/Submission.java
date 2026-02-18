package com.livequiz.backend.domain.submission;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import java.time.Instant;

public class Submission {

  private final SubmissionId id;
  private final LectureId lectureId;
  private final QuestionId questionId;
  private final String studentId; // Placeholder for StudentId if we don't have a Student aggregate yet
  private final Instant timestamp;
  private final String answerText;
  private Feedback feedback;

  public Submission(
    SubmissionId id,
    LectureId lectureId,
    QuestionId questionId,
    String studentId,
    Instant timestamp,
    String answerText
  ) {
    if (id == null) {
      throw new IllegalArgumentException("Submission ID cannot be null");
    }
    if (lectureId == null) {
      throw new IllegalArgumentException("Lecture ID cannot be null");
    }
    if (questionId == null) {
      throw new IllegalArgumentException("Question ID cannot be null");
    }
    if (studentId == null || studentId.isBlank()) {
      throw new IllegalArgumentException("Student ID cannot be null or blank");
    }
    if (timestamp == null) {
      throw new IllegalArgumentException("Timestamp cannot be null");
    }
    if (answerText == null || answerText.isBlank()) {
      throw new IllegalArgumentException("Answer text cannot be null or blank");
    }
    this.id = id;
    this.lectureId = lectureId;
    this.questionId = questionId;
    this.studentId = studentId;
    this.timestamp = timestamp;
    this.answerText = answerText;
    this.feedback = null; // Initially no feedback
  }

  public void provideFeedback(Feedback feedback) {
    this.feedback = feedback;
  }

  public SubmissionId id() {
    return id;
  }

  public LectureId lectureId() {
    return lectureId;
  }

  public QuestionId questionId() {
    return questionId;
  }

  public String studentId() {
    return studentId;
  }

  public Instant timestamp() {
    return timestamp;
  }

  public String answerText() {
    return answerText;
  }

  public Feedback feedback() {
    return feedback;
  }
}
