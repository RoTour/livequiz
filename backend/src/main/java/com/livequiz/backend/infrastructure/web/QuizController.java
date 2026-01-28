package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.application.CreateQuizUseCase;
import com.livequiz.backend.domain.quiz.Quiz;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

  private final CreateQuizUseCase createQuizUseCase;

  public QuizController(CreateQuizUseCase createQuizUseCase) {
    this.createQuizUseCase = createQuizUseCase;
  }

  public record CreateQuizRequestDTO(String quizId, String title) {}

  @PostMapping
  public Map<String, String> createQuiz(
    @RequestBody CreateQuizRequestDTO createQuizRequestDTO
  ) {
    Quiz quiz = this.createQuizUseCase.createQuiz(
      createQuizRequestDTO.quizId(),
      createQuizRequestDTO.title()
    );
    return Map.of("quizId", quiz.id().value());
  }
}
