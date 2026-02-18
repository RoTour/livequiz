package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetNextQuestionForStudentUseCase {

  public record NextQuestionResult(
    String lectureId,
    String questionId,
    String prompt,
    int order,
    int timeLimitSeconds
  ) {}

  private final LectureRepository lectureRepository;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final SubmissionRepository submissionRepository;

  public GetNextQuestionForStudentUseCase(
    LectureRepository lectureRepository,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    SubmissionRepository submissionRepository
  ) {
    this.lectureRepository = lectureRepository;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.submissionRepository = submissionRepository;
  }

  public Optional<NextQuestionResult> execute(String lectureId, String studentId) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    Lecture lecture = this.lectureRepository
      .findById(resolvedLectureId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    ensureStudentIsEnrolled(resolvedLectureId, studentId);

    Set<String> submittedQuestionIds = this.submissionRepository.findSubmittedQuestionIdsByLectureAndStudent(
      resolvedLectureId,
      studentId
    );

    return lecture
      .questions()
      .stream()
      .sorted(Comparator.comparingInt(Question::order))
      .filter(question -> lecture.unlockedQuestionIds().contains(question.id().value()))
      .filter(question -> !submittedQuestionIds.contains(question.id().value()))
      .findFirst()
      .map(question ->
        new NextQuestionResult(
          lecture.id().value(),
          question.id().value(),
          question.prompt(),
          question.order(),
          question.timeLimitSeconds()
        )
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
}
