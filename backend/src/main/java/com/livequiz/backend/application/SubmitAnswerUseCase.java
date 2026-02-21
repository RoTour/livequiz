package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import com.livequiz.backend.infrastructure.web.SubmissionCooldownException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SubmitAnswerUseCase {

  public record SubmitResult(
    String submissionId,
    String lectureId,
    String questionId,
    String studentId,
    String answerStatus
  ) {}

  private final LectureRepository lectureRepository;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final SubmissionRepository submissionRepository;
  private final LiveQuizProperties liveQuizProperties;

  public SubmitAnswerUseCase(
    LectureRepository lectureRepository,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    SubmissionRepository submissionRepository,
    LiveQuizProperties liveQuizProperties
  ) {
    this.lectureRepository = lectureRepository;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.submissionRepository = submissionRepository;
    this.liveQuizProperties = liveQuizProperties;
  }

  public SubmitResult execute(
    String lectureId,
    String questionId,
    String studentId,
    String answerText
  ) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    QuestionId resolvedQuestionId = new QuestionId(questionId);

    Lecture lecture = this.lectureRepository
      .findById(resolvedLectureId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    ensureStudentIsEnrolled(resolvedLectureId, studentId);
    ensureQuestionIsUnlocked(lecture, resolvedQuestionId);
    enforceCooldown(resolvedLectureId, resolvedQuestionId, studentId);

    Submission submission = new Submission(
      new SubmissionId(UUID.randomUUID().toString()),
      resolvedLectureId,
      resolvedQuestionId,
      studentId,
      Instant.now(),
      answerText
    );
    this.submissionRepository.save(submission);
    String answerStatus = AnswerEvaluationStatus.AWAITING_EVALUATION.name();
    return new SubmitResult(
      submission.id().value(),
      lectureId,
      questionId,
      studentId,
      answerStatus
    );
  }

  private void ensureStudentIsEnrolled(LectureId lectureId, String studentId) {
    boolean enrolled = this.lectureEnrollmentRepository.existsByLectureIdAndStudentId(
      lectureId,
      studentId
    );
    if (!enrolled) {
      throw new ApiException(
        HttpStatus.FORBIDDEN,
        "LECTURE_ENROLLMENT_REQUIRED",
        "Student must be enrolled in lecture"
      );
    }
  }

  private void ensureQuestionIsUnlocked(Lecture lecture, QuestionId questionId) {
    Question question = lecture
      .questions()
      .stream()
      .filter(existingQuestion -> existingQuestion.id().value().equals(questionId.value()))
      .findFirst()
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "QUESTION_NOT_FOUND", "Question not found")
      );

    if (!lecture.unlockedQuestionIds().contains(question.id().value())) {
      throw new ApiException(
        HttpStatus.FORBIDDEN,
        "QUESTION_LOCKED",
        "Question is still locked"
      );
    }
  }

  private void enforceCooldown(LectureId lectureId, QuestionId questionId, String studentId) {
    this.submissionRepository
      .findLatestByLectureQuestionAndStudent(lectureId, questionId, studentId)
      .ifPresent(lastSubmission -> {
        Instant nextAllowedSubmissionAt = lastSubmission
          .timestamp()
          .plusSeconds(this.liveQuizProperties.submissionCooldownSeconds());
        Instant now = Instant.now();
        if (now.isBefore(nextAllowedSubmissionAt)) {
          long retryAfterSeconds = Duration.between(now, nextAllowedSubmissionAt).toSeconds() + 1;
          throw new SubmissionCooldownException(retryAfterSeconds);
        }
      });
  }
}
