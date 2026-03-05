package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetStudentAnswerStatusesUseCase {

  public record AnswerStatusResult(
    String lectureId,
    String questionId,
    String prompt,
    int order,
    String status,
    Instant submittedAt
  ) {}

  private final LectureRepository lectureRepository;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final SubmissionRepository submissionRepository;

  public GetStudentAnswerStatusesUseCase(
    LectureRepository lectureRepository,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    SubmissionRepository submissionRepository
  ) {
    this.lectureRepository = lectureRepository;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.submissionRepository = submissionRepository;
  }

  public java.util.List<AnswerStatusResult> execute(String lectureId, String studentId) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    Lecture lecture = this.lectureRepository
      .findById(resolvedLectureId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    ensureStudentIsEnrolled(resolvedLectureId, studentId);

    return lecture
      .questions()
      .stream()
      .sorted(Comparator.comparingInt(Question::order))
      .map(question -> {
        java.util.List<Submission> submissions = this.submissionRepository.findByLectureQuestionAndStudent(
          resolvedLectureId,
          question.id(),
          studentId
        );
        return submissions
          .stream()
          .max(Comparator.comparing(Submission::timestamp))
          .map(latestSubmission -> {
            String status = this.resolveStudentVisibleStatus(submissions);
            return new AnswerStatusResult(
              lecture.id().value(),
              question.id().value(),
              question.prompt(),
              question.order(),
              status,
              latestSubmission.timestamp()
            );
          });
      })
      .flatMap(Optional::stream)
      .toList();
  }

  private String resolveStudentVisibleStatus(java.util.List<Submission> submissions) {
    return submissions
      .stream()
      .filter(Submission::reviewPublished)
      .max(
        Comparator.comparing(submission ->
          submission.evaluationCompletedAt() != null
            ? submission.evaluationCompletedAt()
            : submission.timestamp()
        )
      )
      .map(Submission::answerStatus)
      .orElse(AnswerEvaluationStatus.AWAITING_REVIEW.name());
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
}
