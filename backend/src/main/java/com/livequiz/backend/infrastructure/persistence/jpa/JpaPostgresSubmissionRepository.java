package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.submission.Feedback;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresSubmissionRepository implements SubmissionRepository {

  private final JpaSubmissionRepository jpaSubmissionRepository;

  public JpaPostgresSubmissionRepository(JpaSubmissionRepository jpaSubmissionRepository) {
    this.jpaSubmissionRepository = jpaSubmissionRepository;
  }

  @Override
  public void save(Submission submission) {
    this.jpaSubmissionRepository.save(
        new SubmissionEntity(
          submission.id().value(),
          submission.lectureId().value(),
          submission.questionId().value(),
          submission.studentId(),
          submission.timestamp(),
          submission.answerText(),
          submission.answerStatus(),
          submission.evaluationCompletedAt(),
          submission.reviewPublished(),
          submission.reviewCreatedAt(),
          submission.reviewPublishedAt(),
          submission.reviewedByInstructorId(),
          submission.reviewOrigin(),
          submission.feedback() == null ? null : submission.feedback().isCorrect(),
          submission.feedback() == null ? null : submission.feedback().comment(),
          serializeMissingKeyPoints(submission.feedback()),
          submission.llmSuggestedStatus(),
          submission.llmSuggestedFeedback() == null ? null : submission.llmSuggestedFeedback().comment(),
          serializeMissingKeyPoints(submission.llmSuggestedFeedback()),
          submission.llmSuggestedAt(),
          submission.llmSuggestedModel(),
          submission.llmAcceptedAt(),
          submission.llmAcceptedByInstructorId()
        )
      );
  }

  @Override
  public Optional<Submission> findById(SubmissionId submissionId) {
    return this.jpaSubmissionRepository.findById(submissionId.value()).map(this::toDomain);
  }

  @Override
  public Optional<Submission> findLatestByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  ) {
    return this.jpaSubmissionRepository
      .findTopByLectureIdAndQuestionIdAndStudentIdOrderBySubmittedAtDesc(
        lectureId.value(),
        questionId.value(),
        studentId
      )
      .map(entity ->
        toDomain(entity)
      );
  }

  @Override
  public java.util.List<Submission> findByLectureAndQuestion(
    LectureId lectureId,
    QuestionId questionId
  ) {
    return this.jpaSubmissionRepository
      .findByLectureIdAndQuestionId(lectureId.value(), questionId.value())
      .stream()
      .map(this::toDomain)
      .toList();
  }

  @Override
  public java.util.List<Submission> findByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  ) {
    return this.jpaSubmissionRepository
      .findByLectureIdAndQuestionIdAndStudentIdOrderBySubmittedAtDesc(
        lectureId.value(),
        questionId.value(),
        studentId
      )
      .stream()
      .map(this::toDomain)
      .toList();
  }

  @Override
  public long countByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  ) {
    return this.jpaSubmissionRepository.countByLectureIdAndQuestionIdAndStudentId(
        lectureId.value(),
        questionId.value(),
        studentId
      );
  }

  @Override
  public Set<String> findSubmittedQuestionIdsByLectureAndStudent(
    LectureId lectureId,
    String studentId
  ) {
    return this.jpaSubmissionRepository
      .findDistinctQuestionIdsByLectureAndStudent(lectureId.value(), studentId)
      .stream()
      .collect(Collectors.toSet());
  }

  @Override
  public java.util.List<QuestionStudentAttempt> findQuestionStudentAttemptsByLecture(
    LectureId lectureId
  ) {
    return this.jpaSubmissionRepository
      .findQuestionStudentAttemptsByLecture(lectureId.value())
      .stream()
      .map(projection ->
        new QuestionStudentAttempt(
          projection.getQuestionId(),
          projection.getStudentId(),
          projection.getAttemptCount()
        )
      )
      .toList();
  }

  private Submission toDomain(SubmissionEntity entity) {
    return new Submission(
      new SubmissionId(entity.getId()),
      new LectureId(entity.getLectureId()),
      new QuestionId(entity.getQuestionId()),
      entity.getStudentId(),
      entity.getSubmittedAt(),
      entity.getAnswerText(),
      entity.getAnswerStatus(),
      entity.getEvaluationCompletedAt(),
      mapFeedback(entity),
      entity.isReviewPublished(),
      entity.getReviewCreatedAt(),
      entity.getReviewPublishedAt(),
      entity.getReviewedByInstructorId(),
      entity.getReviewOrigin(),
      entity.getLlmSuggestedStatus(),
      mapLlmSuggestedFeedback(entity),
      entity.getLlmSuggestedAt(),
      entity.getLlmSuggestedModel(),
      entity.getLlmAcceptedAt(),
      entity.getLlmAcceptedByInstructorId()
    );
  }

  private Feedback mapFeedback(SubmissionEntity entity) {
    if (entity.getFeedbackIsCorrect() == null) {
      return null;
    }
    return new Feedback(
      entity.getFeedbackIsCorrect(),
      deserializeMissingKeyPoints(entity.getFeedbackMissingKeyPoints()),
      entity.getFeedbackComment()
    );
  }

  private Feedback mapLlmSuggestedFeedback(SubmissionEntity entity) {
    if (entity.getLlmSuggestedStatus() == null || entity.getLlmSuggestedStatus().isBlank()) {
      return null;
    }
    return new Feedback(
      "CORRECT".equals(entity.getLlmSuggestedStatus()),
      deserializeMissingKeyPoints(entity.getLlmSuggestedMissingKeyPoints()),
      entity.getLlmSuggestedComment()
    );
  }

  private String serializeMissingKeyPoints(Feedback feedback) {
    if (feedback == null || feedback.missingKeyPoints().isEmpty()) {
      return null;
    }
    return String.join("\n", feedback.missingKeyPoints());
  }

  private List<String> deserializeMissingKeyPoints(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(raw.split("\\n")).filter(value -> !value.isBlank()).toList();
  }
}
