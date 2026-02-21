package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetQuestionStudentAnswerHistoryUseCase {

  public record StudentAnswerHistory(
    String studentId,
    Instant latestAnswerAt,
    long attemptCount,
    String latestAnswerText
  ) {}

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final SubmissionRepository submissionRepository;

  public GetQuestionStudentAnswerHistoryUseCase(
    InstructorLectureAccessService instructorLectureAccessService,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    SubmissionRepository submissionRepository
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
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

    return this.lectureEnrollmentRepository
      .findStudentIdsByLectureId(lecture.id())
      .stream()
      .sorted()
      .map(studentId -> {
        long attemptCount = this.submissionRepository.countByLectureQuestionAndStudent(
            lecture.id(),
            resolvedQuestionId,
            studentId
          );
        var latestSubmission = this.submissionRepository.findLatestByLectureQuestionAndStudent(
          lecture.id(),
          resolvedQuestionId,
          studentId
        );
        return new StudentAnswerHistory(
          studentId,
          latestSubmission.map(submission -> submission.timestamp()).orElse(null),
          attemptCount,
          latestSubmission.map(submission -> submission.answerText()).orElse(null)
        );
      })
      .toList();
  }
}
