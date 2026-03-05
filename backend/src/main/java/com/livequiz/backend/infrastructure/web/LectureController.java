package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.application.AddQuestionToLectureUseCase;
import com.livequiz.backend.application.CreateLectureUseCase;
import com.livequiz.backend.application.GetLectureQuestionAnalyticsUseCase;
import com.livequiz.backend.application.GetLectureStateUseCase;
import com.livequiz.backend.application.GetQuestionStudentAnswerHistoryUseCase;
import com.livequiz.backend.application.ListInstructorLecturesUseCase;
import com.livequiz.backend.application.UnlockNextQuestionUseCase;
import com.livequiz.backend.application.UnlockQuestionUseCase;
import com.livequiz.backend.domain.lecture.Lecture;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@CrossOrigin(origins = "*")
public class LectureController {

  private final CreateLectureUseCase createLectureUseCase;
  private final AddQuestionToLectureUseCase addQuestionToLectureUseCase;
  private final UnlockQuestionUseCase unlockQuestionUseCase;
  private final UnlockNextQuestionUseCase unlockNextQuestionUseCase;
  private final GetLectureStateUseCase getLectureStateUseCase;
  private final GetLectureQuestionAnalyticsUseCase getLectureQuestionAnalyticsUseCase;
  private final GetQuestionStudentAnswerHistoryUseCase getQuestionStudentAnswerHistoryUseCase;
  private final ListInstructorLecturesUseCase listInstructorLecturesUseCase;

  public LectureController(
    CreateLectureUseCase createLectureUseCase,
    AddQuestionToLectureUseCase addQuestionToLectureUseCase,
    UnlockQuestionUseCase unlockQuestionUseCase,
    UnlockNextQuestionUseCase unlockNextQuestionUseCase,
    GetLectureStateUseCase getLectureStateUseCase,
    GetLectureQuestionAnalyticsUseCase getLectureQuestionAnalyticsUseCase,
    GetQuestionStudentAnswerHistoryUseCase getQuestionStudentAnswerHistoryUseCase,
    ListInstructorLecturesUseCase listInstructorLecturesUseCase
  ) {
    this.createLectureUseCase = createLectureUseCase;
    this.addQuestionToLectureUseCase = addQuestionToLectureUseCase;
    this.unlockQuestionUseCase = unlockQuestionUseCase;
    this.unlockNextQuestionUseCase = unlockNextQuestionUseCase;
    this.getLectureStateUseCase = getLectureStateUseCase;
    this.getLectureQuestionAnalyticsUseCase = getLectureQuestionAnalyticsUseCase;
    this.getQuestionStudentAnswerHistoryUseCase = getQuestionStudentAnswerHistoryUseCase;
    this.listInstructorLecturesUseCase = listInstructorLecturesUseCase;
  }

  public record CreateLectureRequestDTO(String title) {}
  public record AddQuestionRequestDTO(
    String questionId,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds
  ) {}
  public record InstructorLectureSummaryResponse(
    String lectureId,
    String title,
    String createdAt,
    int questionCount,
    int unlockedCount
  ) {}
  public record QuestionAnalyticsResponse(
    String questionId,
    String prompt,
    int order,
    long enrolledCount,
    long answeredCount,
    long unansweredCount,
    long multiAttemptCount
  ) {}
  public record StudentAnswerHistoryResponse(
    String studentId,
    String studentEmail,
    String latestAnswerAt,
    long attemptCount,
    String latestAnswerText
  ) {}

  @GetMapping
  @LogExecutionTime
  public java.util.List<InstructorLectureSummaryResponse> listInstructorLectures() {
    return this.listInstructorLecturesUseCase
      .execute()
      .stream()
      .map(summary ->
        new InstructorLectureSummaryResponse(
          summary.lectureId(),
          summary.title(),
          summary.createdAt() != null ? summary.createdAt().toString() : null,
          summary.questionCount(),
          summary.unlockedCount()
        )
      )
      .toList();
  }

  @PostMapping
  @LogExecutionTime
  public Map<String, String> createLecture(
    @RequestBody CreateLectureRequestDTO createLectureRequestDTO
  ) {
    Lecture lecture = this.createLectureUseCase.createLecture(createLectureRequestDTO.title());
    return Map.of("lectureId", lecture.id().value());
  }

  @PostMapping("/{lectureId}/questions")
  @LogExecutionTime
  public Map<String, String> addQuestion(
    @PathVariable String lectureId,
    @RequestBody AddQuestionRequestDTO request
  ) {
    String questionId =
      request.questionId() == null || request.questionId().isBlank()
        ? UUID.randomUUID().toString()
        : request.questionId();
    Lecture lecture = this.addQuestionToLectureUseCase.execute(
        lectureId,
        questionId,
        request.prompt(),
        request.modelAnswer(),
        request.timeLimitSeconds()
      );
    return Map.of("lectureId", lecture.id().value(), "questionId", questionId);
  }

  @PostMapping("/{lectureId}/questions/{questionId}/unlock")
  @LogExecutionTime
  public Map<String, String> unlockQuestion(
    @PathVariable String lectureId,
    @PathVariable String questionId
  ) {
    Lecture lecture = this.unlockQuestionUseCase.execute(lectureId, questionId);
    return Map.of("lectureId", lecture.id().value(), "questionId", questionId);
  }

  @PostMapping("/{lectureId}/questions/unlock-next")
  @LogExecutionTime
  public Map<String, String> unlockNextQuestion(@PathVariable String lectureId) {
    Lecture lecture = this.unlockNextQuestionUseCase.execute(lectureId);
    return Map.of("lectureId", lecture.id().value());
  }

  @GetMapping("/{lectureId}/state")
  @LogExecutionTime
  public LectureStateResponse getLectureState(@PathVariable String lectureId) {
    Lecture lecture = this.getLectureStateUseCase.execute(lectureId);
    return new LectureStateResponse(
      lecture.id().value(),
      lecture.title(),
      lecture
        .questions()
        .stream()
        .sorted(java.util.Comparator.comparingInt(com.livequiz.backend.domain.lecture.Question::order))
        .map(question ->
          new QuestionStateResponse(
            question.id().value(),
            question.prompt(),
            question.order(),
            question.timeLimitSeconds(),
            lecture.unlockedQuestionIds().contains(question.id().value())
          )
        )
        .toList()
      );
  }

  @GetMapping("/{lectureId}/questions/analytics")
  @LogExecutionTime
  public java.util.List<QuestionAnalyticsResponse> getLectureQuestionAnalytics(
    @PathVariable String lectureId
  ) {
    return this.getLectureQuestionAnalyticsUseCase
      .execute(lectureId)
      .stream()
      .map(analytics ->
        new QuestionAnalyticsResponse(
          analytics.questionId(),
          analytics.prompt(),
          analytics.order(),
          analytics.enrolledCount(),
          analytics.answeredCount(),
          analytics.unansweredCount(),
          analytics.multiAttemptCount()
        )
      )
      .toList();
  }

  @GetMapping("/{lectureId}/questions/{questionId}/answers/history")
  @LogExecutionTime
  public java.util.List<StudentAnswerHistoryResponse> getQuestionStudentAnswerHistory(
    @PathVariable String lectureId,
    @PathVariable String questionId
  ) {
    return this.getQuestionStudentAnswerHistoryUseCase
      .execute(lectureId, questionId)
      .stream()
      .map(history ->
        new StudentAnswerHistoryResponse(
          history.studentId(),
          history.studentEmail(),
          history.latestAnswerAt() != null ? history.latestAnswerAt().toString() : null,
          history.attemptCount(),
          history.latestAnswerText()
        )
      )
      .toList();
  }

  public record LectureStateResponse(
    String lectureId,
    String title,
    java.util.List<QuestionStateResponse> questions
  ) {}

  public record QuestionStateResponse(
    String questionId,
    String prompt,
    int order,
    int timeLimitSeconds,
    boolean unlocked
  ) {}
}
