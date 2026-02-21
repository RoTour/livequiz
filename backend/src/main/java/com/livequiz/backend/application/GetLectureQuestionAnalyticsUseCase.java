package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GetLectureQuestionAnalyticsUseCase {

  public record QuestionAnalytics(
    String questionId,
    String prompt,
    int order,
    long enrolledCount,
    long answeredCount,
    long unansweredCount,
    long multiAttemptCount
  ) {}

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final SubmissionRepository submissionRepository;

  public GetLectureQuestionAnalyticsUseCase(
    InstructorLectureAccessService instructorLectureAccessService,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    SubmissionRepository submissionRepository
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.submissionRepository = submissionRepository;
  }

  public java.util.List<QuestionAnalytics> execute(String lectureId) {
    Lecture lecture = this.instructorLectureAccessService.getOwnedLectureOrThrow(lectureId);
    long enrolledCount = this.lectureEnrollmentRepository.countByLectureId(lecture.id());

    Map<String, java.util.List<SubmissionRepository.QuestionStudentAttempt>> attemptsByQuestion = this.submissionRepository
      .findQuestionStudentAttemptsByLecture(lecture.id())
      .stream()
      .collect(Collectors.groupingBy(SubmissionRepository.QuestionStudentAttempt::questionId));

    return lecture
      .questions()
      .stream()
      .sorted(java.util.Comparator.comparingInt(com.livequiz.backend.domain.lecture.Question::order))
      .map(question -> {
        java.util.List<SubmissionRepository.QuestionStudentAttempt> attempts = attemptsByQuestion.getOrDefault(
          question.id().value(),
          java.util.List.of()
        );
        long answeredCount = attempts.size();
        long multiAttemptCount = attempts
          .stream()
          .filter(attempt -> attempt.attemptCount() > 1)
          .count();
        long unansweredCount = Math.max(0, enrolledCount - answeredCount);
        return new QuestionAnalytics(
          question.id().value(),
          question.prompt(),
          question.order(),
          enrolledCount,
          answeredCount,
          unansweredCount,
          multiAttemptCount
        );
      })
      .toList();
  }
}
