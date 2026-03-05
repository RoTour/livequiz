package com.livequiz.backend.domain.submission;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Submission {

  public static final String STATUS_AWAITING_REVIEW = "AWAITING_REVIEW";
  public static final String REVIEW_ORIGIN_MANUAL = "MANUAL";
  public static final String REVIEW_ORIGIN_LLM_ACCEPTED = "LLM_ACCEPTED";
  private static final Set<String> ALLOWED_STATUSES = Set.of(
    "AWAITING_REVIEW",
    "CORRECT",
    "NEEDS_IMPROVEMENT",
    "INCOMPLETE"
  );
  private static final Set<String> REVIEWABLE_OUTCOMES = Set.of(
    "CORRECT",
    "NEEDS_IMPROVEMENT",
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
  private boolean reviewPublished;
  private Instant reviewCreatedAt;
  private Instant reviewPublishedAt;
  private String reviewedByInstructorId;
  private String reviewOrigin;
  private String llmSuggestedStatus;
  private Feedback llmSuggestedFeedback;
  private Instant llmSuggestedAt;
  private String llmSuggestedModel;
  private Instant llmAcceptedAt;
  private String llmAcceptedByInstructorId;

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
    this.answerStatus = STATUS_AWAITING_REVIEW;
    this.evaluationCompletedAt = null;
    this.feedback = null; // Initially no feedback
    this.reviewPublished = false;
    this.reviewCreatedAt = null;
    this.reviewPublishedAt = null;
    this.reviewedByInstructorId = null;
    this.reviewOrigin = null;
    this.llmSuggestedStatus = null;
    this.llmSuggestedFeedback = null;
    this.llmSuggestedAt = null;
    this.llmSuggestedModel = null;
    this.llmAcceptedAt = null;
    this.llmAcceptedByInstructorId = null;
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
    validateStatus(answerStatus);
    this.answerStatus = answerStatus;
    this.evaluationCompletedAt = evaluationCompletedAt;
    this.feedback = feedback;
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
    Feedback feedback,
    boolean reviewPublished,
    Instant reviewCreatedAt,
    Instant reviewPublishedAt,
    String reviewedByInstructorId,
    String reviewOrigin,
    String llmSuggestedStatus,
    Feedback llmSuggestedFeedback,
    Instant llmSuggestedAt,
    String llmSuggestedModel,
    Instant llmAcceptedAt,
    String llmAcceptedByInstructorId
  ) {
    this(
      id,
      lectureId,
      questionId,
      studentId,
      timestamp,
      answerText,
      answerStatus,
      evaluationCompletedAt,
      feedback
    );
    this.reviewPublished = reviewPublished;
    this.reviewCreatedAt = reviewCreatedAt;
    this.reviewPublishedAt = reviewPublishedAt;
    this.reviewedByInstructorId = reviewedByInstructorId;
    this.reviewOrigin = reviewOrigin;
    this.llmSuggestedStatus = llmSuggestedStatus;
    this.llmSuggestedFeedback = llmSuggestedFeedback;
    this.llmSuggestedAt = llmSuggestedAt;
    this.llmSuggestedModel = llmSuggestedModel;
    this.llmAcceptedAt = llmAcceptedAt;
    this.llmAcceptedByInstructorId = llmAcceptedByInstructorId;
  }

  public void upsertManualReview(
    String answerStatus,
    List<String> missingKeyPoints,
    String comment,
    String instructorId,
    boolean publish,
    Instant updatedAt
  ) {
    if (!REVIEWABLE_OUTCOMES.contains(answerStatus)) {
      throw new IllegalArgumentException("Manual review status is invalid");
    }
    if (instructorId == null || instructorId.isBlank()) {
      throw new IllegalArgumentException("Instructor ID cannot be null or blank");
    }
    if (updatedAt == null) {
      throw new IllegalArgumentException("Review updated date cannot be null");
    }

    this.answerStatus = answerStatus;
    this.evaluationCompletedAt = updatedAt;
    this.feedback = new Feedback(
      "CORRECT".equals(answerStatus),
      missingKeyPoints == null ? List.of() : new ArrayList<>(missingKeyPoints),
      comment
    );
    this.reviewedByInstructorId = instructorId;
    this.reviewOrigin = REVIEW_ORIGIN_MANUAL;
    if (this.reviewCreatedAt == null) {
      this.reviewCreatedAt = updatedAt;
    }
    this.reviewPublished = publish;
    this.reviewPublishedAt = publish ? updatedAt : null;
  }

  public void recordLlmSuggestion(
    String answerStatus,
    List<String> missingKeyPoints,
    String comment,
    String model,
    Instant suggestedAt
  ) {
    if (!REVIEWABLE_OUTCOMES.contains(answerStatus)) {
      throw new IllegalArgumentException("LLM suggestion status is invalid");
    }
    if (suggestedAt == null) {
      throw new IllegalArgumentException("LLM suggestion date cannot be null");
    }
    this.llmSuggestedStatus = answerStatus;
    this.llmSuggestedFeedback = new Feedback(
      "CORRECT".equals(answerStatus),
      missingKeyPoints == null ? List.of() : new ArrayList<>(missingKeyPoints),
      comment
    );
    this.llmSuggestedAt = suggestedAt;
    this.llmSuggestedModel = model;
  }

  public void acceptLlmSuggestion(String instructorId, boolean publish, Instant acceptedAt) {
    if (this.llmSuggestedStatus == null || this.llmSuggestedStatus.isBlank()) {
      throw new IllegalArgumentException("No LLM suggestion available");
    }
    if (instructorId == null || instructorId.isBlank()) {
      throw new IllegalArgumentException("Instructor ID cannot be null or blank");
    }
    if (acceptedAt == null) {
      throw new IllegalArgumentException("LLM acceptance date cannot be null");
    }

    this.answerStatus = this.llmSuggestedStatus;
    this.feedback = this.llmSuggestedFeedback;
    this.evaluationCompletedAt = acceptedAt;
    this.reviewedByInstructorId = instructorId;
    this.reviewOrigin = REVIEW_ORIGIN_LLM_ACCEPTED;
    if (this.reviewCreatedAt == null) {
      this.reviewCreatedAt = acceptedAt;
    }
    this.reviewPublished = publish;
    this.reviewPublishedAt = publish ? acceptedAt : null;
    this.llmAcceptedAt = acceptedAt;
    this.llmAcceptedByInstructorId = instructorId;
  }

  private void validateStatus(String answerStatus) {
    if (answerStatus == null || answerStatus.isBlank()) {
      throw new IllegalArgumentException("Answer status cannot be null or blank");
    }
    if (!ALLOWED_STATUSES.contains(answerStatus)) {
      throw new IllegalArgumentException("Answer status is invalid");
    }
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

  public boolean reviewPublished() {
    return reviewPublished;
  }

  public Instant reviewCreatedAt() {
    return reviewCreatedAt;
  }

  public Instant reviewPublishedAt() {
    return reviewPublishedAt;
  }

  public String reviewedByInstructorId() {
    return reviewedByInstructorId;
  }

  public String reviewOrigin() {
    return reviewOrigin;
  }

  public String llmSuggestedStatus() {
    return llmSuggestedStatus;
  }

  public Feedback llmSuggestedFeedback() {
    return llmSuggestedFeedback;
  }

  public Instant llmSuggestedAt() {
    return llmSuggestedAt;
  }

  public String llmSuggestedModel() {
    return llmSuggestedModel;
  }

  public Instant llmAcceptedAt() {
    return llmAcceptedAt;
  }

  public String llmAcceptedByInstructorId() {
    return llmAcceptedByInstructorId;
  }
}
