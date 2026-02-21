package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresLectureRepository implements LectureRepository {

  private final JpaLectureRepository jpaLectureRepository;

  public JpaPostgresLectureRepository(JpaLectureRepository jpaLectureRepository) {
    this.jpaLectureRepository = jpaLectureRepository;
  }

  @Override
  public void save(Lecture lecture) {
    List<LectureQuestionEmbeddable> questions = lecture
      .questions()
      .stream()
      .map(question ->
        new LectureQuestionEmbeddable(
          question.id().value(),
          question.prompt(),
          question.modelAnswer(),
          question.timeLimitSeconds(),
          question.order()
        )
      )
      .toList();

    LectureEntity entity = new LectureEntity(
      lecture.id().value(),
      lecture.title(),
      questions,
      lecture.unlockedQuestionIds().stream().toList(),
      lecture.createdByInstructorId(),
      lecture.createdAt()
    );
    this.jpaLectureRepository.save(entity);
  }

  @Override
  public Optional<Lecture> findById(LectureId lectureId) {
    return this.jpaLectureRepository
      .findById(lectureId.value())
      .map(entity ->
        new Lecture(
          new LectureId(entity.getId()),
          entity.getTitle(),
          entity
            .getQuestions()
            .stream()
            .map(question ->
              new Question(
                new QuestionId(question.getQuestionId()),
                question.getPrompt(),
                question.getModelAnswer(),
                question.getTimeLimitSeconds(),
                question.getQuestionOrder(),
                java.util.List.of()
              )
            )
            .toList(),
          new java.util.LinkedHashSet<>(entity.getUnlockedQuestionIds()),
          entity.getCreatedByInstructorId(),
          entity.getCreatedAt()
        )
      );
  }
}
