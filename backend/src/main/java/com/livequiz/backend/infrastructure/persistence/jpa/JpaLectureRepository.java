package com.livequiz.backend.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaLectureRepository extends JpaRepository<LectureEntity, String> {

  @Override
  Optional<LectureEntity> findById(String id);

  List<LectureEntity> findByCreatedByInstructorIdOrderByCreatedAtDesc(String createdByInstructorId);

  @Query(
    value = "select question_id as questionId, prompt as prompt, model_answer as modelAnswer, " +
    "time_limit_seconds as timeLimitSeconds, question_order as questionOrder " +
    "from lecture_questions where lecture_id = :lectureId order by question_order",
    nativeQuery = true
  )
  List<LectureQuestionRow> findQuestionRowsByLectureId(@Param("lectureId") String lectureId);

  @Query(
    value = "select question_id from lecture_unlocked_questions where lecture_id = :lectureId",
    nativeQuery = true
  )
  List<String> findUnlockedQuestionIdsByLectureId(@Param("lectureId") String lectureId);

  interface LectureQuestionRow {
    String getQuestionId();

    String getPrompt();

    String getModelAnswer();

    Integer getTimeLimitSeconds();

    Integer getQuestionOrder();
  }
}
