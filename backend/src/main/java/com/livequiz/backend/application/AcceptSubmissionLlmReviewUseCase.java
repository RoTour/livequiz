package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptSubmissionLlmReviewUseCase {

  public record Result(
    String submissionId,
    String reviewStatus,
    boolean reviewPublished,
    Instant reviewUpdatedAt,
    String reviewedByInstructorId,
    Instant llmAcceptedAt
  ) {}

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final CurrentUserService currentUserService;
  private final SubmissionRepository submissionRepository;

  public AcceptSubmissionLlmReviewUseCase(
    InstructorLectureAccessService instructorLectureAccessService,
    CurrentUserService currentUserService,
    SubmissionRepository submissionRepository
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
    this.currentUserService = currentUserService;
    this.submissionRepository = submissionRepository;
  }

  @Transactional
  public Result execute(String lectureId, String questionId, String submissionId, boolean published) {
    Lecture lecture = this.instructorLectureAccessService.getOwnedLectureOrThrow(lectureId);
    QuestionId resolvedQuestionId = new QuestionId(questionId);
    boolean questionExists = lecture
      .questions()
      .stream()
      .anyMatch(question -> question.id().value().equals(resolvedQuestionId.value()));
    if (!questionExists) {
      throw new ApiException(
        HttpStatus.NOT_FOUND,
        "QUESTION_NOT_FOUND",
        "Question not found"
      );
    }

    Submission submission = this.submissionRepository
      .findById(new SubmissionId(submissionId))
      .orElseThrow(() ->
        new ApiException(
          HttpStatus.NOT_FOUND,
          "SUBMISSION_NOT_FOUND",
          "Submission not found"
        )
      );
    if (!submission.lectureId().value().equals(lecture.id().value())) {
      throw new ApiException(
        HttpStatus.NOT_FOUND,
        "SUBMISSION_NOT_FOUND",
        "Submission not found"
      );
    }
    if (!submission.questionId().value().equals(resolvedQuestionId.value())) {
      throw new ApiException(
        HttpStatus.NOT_FOUND,
        "SUBMISSION_NOT_FOUND",
        "Submission not found"
      );
    }

    String instructorId = this.currentUserService.requireUserId();
    try {
      submission.acceptLlmSuggestion(instructorId, published, Instant.now());
    } catch (IllegalArgumentException exception) {
      throw new ApiException(
        HttpStatus.BAD_REQUEST,
        "LLM_REVIEW_NOT_AVAILABLE",
        exception.getMessage()
      );
    }
    this.submissionRepository.save(submission);
    return new Result(
      submission.id().value(),
      submission.answerStatus(),
      submission.reviewPublished(),
      submission.evaluationCompletedAt(),
      submission.reviewedByInstructorId(),
      submission.llmAcceptedAt()
    );
  }
}
