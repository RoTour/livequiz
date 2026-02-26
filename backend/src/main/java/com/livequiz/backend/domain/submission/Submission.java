package com.livequiz.backend.domain.submission;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Submission {

  public static final String STATUS_AWAITING_EVALUATION = "AWAITING_EVALUATION";
  private static final Set<String> ALLOWED_STATUSES = Set.of(
    "AWAITING_EVALUATION",
    "CORRECT",
    "INCORRECT",
    "INCOMPLETE"
  );

  private final SubmissionId id;
  private final LectureId lectureId;
  private final QuestionId questionId;
  private final String studentId; // Placeholder for StudentId if we don't have a Student aggregate yet
  private final Instant timestamp;
  private final String answerText;
  private String answerStatus;
  private Instant evaluationCompletedAt;
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
    this.answerStatus = STATUS_AWAITING_EVALUATION;
    this.evaluationCompletedAt = null;
    this.feedback = null; // Initially no feedback
  }

  public Submission(
    SubmissionId id,
    LectureId lectureId,
    QuestionId questionId,
    String studentId,
    Instant timestamp,
    String answerText,
    String answerStatus,
    Instant evaluationCompletedAt,
    Feedback feedback
  ) {
    this(id, lectureId, questionId, studentId, timestamp, answerText);
    if (answerStatus == null || answerStatus.isBlank()) {
      throw new IllegalArgumentException("Answer status cannot be null or blank");
    }
    if (!ALLOWED_STATUSES.contains(answerStatus)) {
      throw new IllegalArgumentException("Answer status is invalid");
    }
    this.answerStatus = answerStatus;
    this.evaluationCompletedAt = evaluationCompletedAt;
    this.feedback = feedback;
  }

  public void provideFeedback(Feedback feedback) {
    this.feedback = feedback;
  }

  public void applyEvaluation(
    String answerStatus,
    boolean isCorrect,
    List<String> missingKeyPoints,
    String comment,
    Instant completedAt
  ) {
    if (answerStatus == null || answerStatus.isBlank()) {
      throw new IllegalArgumentException("Answer status cannot be null or blank");
    }
    if (!ALLOWED_STATUSES.contains(answerStatus)) {
      throw new IllegalArgumentException("Answer status is invalid");
    }
    if (completedAt == null) {
      throw new IllegalArgumentException("Evaluation completed date cannot be null");
    }
    this.answerStatus = answerStatus;
    this.evaluationCompletedAt = completedAt;
    this.feedback = new Feedback(
      isCorrect,
      missingKeyPoints == null ? List.of() : new ArrayList<>(missingKeyPoints),
      comment
    );
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

  public String answerStatus() {
    return answerStatus;
  }

  public Instant evaluationCompletedAt() {
    return evaluationCompletedAt;
  }

  public Feedback feedback() {
    return feedback;
  }
}
