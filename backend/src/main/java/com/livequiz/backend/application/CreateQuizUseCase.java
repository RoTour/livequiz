package com.livequiz.backend.application;

import com.livequiz.backend.domain.quiz.Quiz;
import com.livequiz.backend.domain.quiz.QuizId;
import com.livequiz.backend.domain.quiz.QuizRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateQuizUseCase {

  private final QuizRepository quizRepository;

  public CreateQuizUseCase(QuizRepository quizRepository) {
    this.quizRepository = quizRepository;
  }

  public Quiz createQuiz(String quizId, String title) {
    if (quizId == null || quizId.isBlank()) {
      quizId = UUID.randomUUID().toString();
    }
    Quiz quiz = new Quiz(new QuizId(quizId), title);
    this.quizRepository.save(quiz);
    return quiz;
  }
}
