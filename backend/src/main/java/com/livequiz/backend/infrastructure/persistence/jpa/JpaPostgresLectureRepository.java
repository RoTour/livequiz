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
import org.springframework.transaction.annotation.Transactional;

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
      new java.util.LinkedHashSet<>(lecture.unlockedQuestionIds()),
      lecture.createdByInstructorId(),
      lecture.createdAt()
    );
    this.jpaLectureRepository.save(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Lecture> findById(LectureId lectureId) {
    return this.jpaLectureRepository.findById(lectureId.value()).map(entity ->
      toDomain(
        entity,
        this.jpaLectureRepository.findQuestionRowsByLectureId(entity.getId()),
        this.jpaLectureRepository.findUnlockedQuestionIdsByLectureId(entity.getId())
      )
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<Lecture> findByCreatedByInstructorId(String createdByInstructorId) {
    return this.jpaLectureRepository
      .findByCreatedByInstructorIdOrderByCreatedAtDesc(createdByInstructorId)
      .stream()
      .map(entity ->
        toDomain(
          entity,
          this.jpaLectureRepository.findQuestionRowsByLectureId(entity.getId()),
          this.jpaLectureRepository.findUnlockedQuestionIdsByLectureId(entity.getId())
        )
      )
      .toList();
  }

  private Lecture toDomain(
    LectureEntity entity,
    List<JpaLectureRepository.LectureQuestionRow> questions,
    List<String> unlockedQuestionIds
  ) {
    return new Lecture(
      new LectureId(entity.getId()),
      entity.getTitle(),
      questions
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
      new java.util.LinkedHashSet<>(unlockedQuestionIds),
      entity.getCreatedByInstructorId(),
      entity.getCreatedAt()
    );
  }
}
