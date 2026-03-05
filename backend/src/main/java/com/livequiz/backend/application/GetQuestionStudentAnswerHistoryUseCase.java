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
public class GetQuestionStudentAnswerHistoryUseCase {

  public record StudentAnswerHistory(
    String studentId,
    String studentEmail,
    Instant latestAnswerAt,
    long attemptCount,
    String latestAnswerText
  ) {}

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final StudentIdentityRepository studentIdentityRepository;
  private final SubmissionRepository submissionRepository;

  public GetQuestionStudentAnswerHistoryUseCase(
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

  public java.util.List<StudentAnswerHistory> execute(String lectureId, String questionId) {
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

    Map<String, java.util.List<Submission>> submissionsByStudent = this.submissionRepository
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
        java.util.List<Submission> submissions = submissionsByStudent.getOrDefault(
          studentId,
          java.util.List.of()
        );
        long attemptCount = submissions.size();
        var latestSubmission = submissions
          .stream()
          .max(Comparator.comparing(Submission::timestamp));
        return new StudentAnswerHistory(
          studentId,
          verifiedStudentEmails.get(studentId),
          latestSubmission.map(submission -> submission.timestamp()).orElse(null),
          attemptCount,
          latestSubmission.map(submission -> submission.answerText()).orElse(null)
        );
      })
      .toList();
  }
}
