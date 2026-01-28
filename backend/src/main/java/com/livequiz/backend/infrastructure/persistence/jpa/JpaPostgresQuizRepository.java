package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.quiz.Quiz;
import com.livequiz.backend.domain.quiz.QuizRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresQuizRepository implements QuizRepository {

  private final JpaQuizRepository jpaQuizRepository;

  public JpaPostgresQuizRepository(JpaQuizRepository jpaQuizRepository) {
    this.jpaQuizRepository = jpaQuizRepository;
  }

  @Override
  public void save(Quiz quiz) {
    QuizEntity entity = new QuizEntity(quiz.id().value(), quiz.title());
    this.jpaQuizRepository.save(entity);
  }
}
