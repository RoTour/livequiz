package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.application.CurrentUserService;
import com.livequiz.backend.application.GetNextQuestionForStudentUseCase;
import com.livequiz.backend.application.JoinLectureUseCase;
import com.livequiz.backend.application.SubmitAnswerUseCase;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
public class StudentLectureController {

  private final JoinLectureUseCase joinLectureUseCase;
  private final GetNextQuestionForStudentUseCase getNextQuestionForStudentUseCase;
  private final SubmitAnswerUseCase submitAnswerUseCase;
  private final CurrentUserService currentUserService;

  public StudentLectureController(
    JoinLectureUseCase joinLectureUseCase,
    GetNextQuestionForStudentUseCase getNextQuestionForStudentUseCase,
    SubmitAnswerUseCase submitAnswerUseCase,
    CurrentUserService currentUserService
  ) {
    this.joinLectureUseCase = joinLectureUseCase;
    this.getNextQuestionForStudentUseCase = getNextQuestionForStudentUseCase;
    this.submitAnswerUseCase = submitAnswerUseCase;
    this.currentUserService = currentUserService;
  }

  public record JoinLectureRequest(String token, String code) {}

  public record SubmitAnswerRequest(String questionId, String answerText) {}

  @PostMapping("/join")
  public JoinLectureUseCase.JoinResult joinLecture(
    @RequestBody JoinLectureRequest request
  ) {
    String studentId = this.currentUserService.requireUserId();
    return this.joinLectureUseCase.execute(request.token(), request.code(), studentId);
  }

  @GetMapping("/{lectureId}/students/me/next-question")
  public Map<String, Object> getNextQuestion(@PathVariable String lectureId) {
    String studentId = this.currentUserService.requireUserId();
    return this.getNextQuestionForStudentUseCase
      .execute(lectureId, studentId)
      .<Map<String, Object>>map(nextQuestion ->
        Map.of(
          "hasQuestion",
          true,
          "lectureId",
          nextQuestion.lectureId(),
          "questionId",
          nextQuestion.questionId(),
          "prompt",
          nextQuestion.prompt(),
          "order",
          nextQuestion.order(),
          "timeLimitSeconds",
          nextQuestion.timeLimitSeconds()
        )
      )
      .orElseGet(() -> Map.of("hasQuestion", false));
  }

  @PostMapping("/{lectureId}/submissions")
  public SubmitAnswerUseCase.SubmitResult submitAnswer(
    @PathVariable String lectureId,
    @RequestBody SubmitAnswerRequest request
  ) {
    String studentId = this.currentUserService.requireUserId();
    return this.submitAnswerUseCase.execute(
        lectureId,
        request.questionId(),
        studentId,
        request.answerText()
      );
  }
}
