package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.quiz.Quiz;
import com.livequiz.backend.domain.quiz.QuizRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("in-memory")
public class InMemoryQuizRepository implements QuizRepository {

  public final Map<String, Quiz> quizzes = new ConcurrentHashMap<>();

  @Override
  public void save(Quiz quiz) {
    this.quizzes.put(quiz.id().value(), quiz);
  }
}
