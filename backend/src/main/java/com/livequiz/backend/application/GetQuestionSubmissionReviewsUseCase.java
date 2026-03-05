package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.domain.student.StudentIdentityStatus;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetQuestionSubmissionReviewsUseCase {

  public record SubmissionAttemptReview(
    String submissionId,
    Instant answeredAt,
    String answerText,
    String reviewStatus,
    boolean reviewPublished,
    String reviewComment,
    Instant reviewUpdatedAt,
    Instant reviewCreatedAt,
    Instant reviewPublishedAt,
    String reviewedByInstructorId,
    String reviewOrigin,
    String llmSuggestedStatus,
    String llmSuggestedComment,
    Instant llmSuggestedAt,
    String llmSuggestedModel,
    Instant llmAcceptedAt,
    String llmAcceptedByInstructorId
  ) {}

  public record StudentSubmissionReviews(
    String studentId,
    String studentEmail,
    List<SubmissionAttemptReview> attempts
  ) {}

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final StudentIdentityRepository studentIdentityRepository;
  private final SubmissionRepository submissionRepository;

  public GetQuestionSubmissionReviewsUseCase(
    InstructorLectureAccessService instructorLectureAccessService,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    StudentIdentityRepository studentIdentityRepository,
    SubmissionRepository submissionRepository
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.studentIdentityRepository = studentIdentityRepository;
    this.submissionRepository = submissionRepository;
  }

  public List<StudentSubmissionReviews> execute(String lectureId, String questionId) {
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

    Map<String, List<Submission>> submissionsByStudent = this.submissionRepository
      .findByLectureAndQuestion(lecture.id(), resolvedQuestionId)
      .stream()
      .collect(Collectors.groupingBy(Submission::studentId));

    List<String> enrolledStudentIds = this.lectureEnrollmentRepository
      .findStudentIdsByLectureId(lecture.id())
      .stream()
      .sorted()
      .toList();

    Map<String, String> verifiedStudentEmails = this.studentIdentityRepository
      .findByStudentIds(enrolledStudentIds)
      .stream()
      .filter(identity -> identity.status() == StudentIdentityStatus.REGISTERED_VERIFIED)
      .collect(
        Collectors.toMap(StudentIdentity::studentId, StudentIdentity::email, (first, ignored) -> first)
      );

    return enrolledStudentIds
      .stream()
      .map(studentId -> {
        List<SubmissionAttemptReview> attempts = submissionsByStudent
          .getOrDefault(studentId, List.of())
          .stream()
          .sorted(Comparator.comparing(Submission::timestamp).reversed())
          .map(this::toAttemptReview)
          .toList();
        return new StudentSubmissionReviews(studentId, verifiedStudentEmails.get(studentId), attempts);
      })
      .toList();
  }

  private SubmissionAttemptReview toAttemptReview(Submission submission) {
    return new SubmissionAttemptReview(
      submission.id().value(),
      submission.timestamp(),
      submission.answerText(),
      submission.answerStatus(),
      submission.reviewPublished(),
      submission.feedback() == null ? null : submission.feedback().comment(),
      submission.evaluationCompletedAt(),
      submission.reviewCreatedAt(),
      submission.reviewPublishedAt(),
      submission.reviewedByInstructorId(),
      submission.reviewOrigin(),
      submission.llmSuggestedStatus(),
      submission.llmSuggestedFeedback() == null ? null : submission.llmSuggestedFeedback().comment(),
      submission.llmSuggestedAt(),
      submission.llmSuggestedModel(),
      submission.llmAcceptedAt(),
      submission.llmAcceptedByInstructorId()
    );
  }
}
