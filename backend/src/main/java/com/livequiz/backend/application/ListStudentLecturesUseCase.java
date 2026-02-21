package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ListStudentLecturesUseCase {

  public record StudentLectureSummary(
    String lectureId,
    String title,
    Instant enrolledAt,
    int questionCount,
    int answeredCount
  ) {}

  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final LectureRepository lectureRepository;
  private final SubmissionRepository submissionRepository;

  public ListStudentLecturesUseCase(
    LectureEnrollmentRepository lectureEnrollmentRepository,
    LectureRepository lectureRepository,
    SubmissionRepository submissionRepository
  ) {
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.lectureRepository = lectureRepository;
    this.submissionRepository = submissionRepository;
  }

  public java.util.List<StudentLectureSummary> execute(String studentId) {
    return this.lectureEnrollmentRepository
      .findByStudentId(studentId)
      .stream()
      .map(enrollment ->
        this.lectureRepository
          .findById(enrollment.lectureId())
          .map(lecture ->
            new StudentLectureSummary(
              lecture.id().value(),
              lecture.title(),
              enrollment.enrolledAt(),
              lecture.questions().size(),
              this.submissionRepository
                .findSubmittedQuestionIdsByLectureAndStudent(lecture.id(), studentId)
                .size()
            )
          )
      )
      .flatMap(java.util.Optional::stream)
      .toList();
  }
}
