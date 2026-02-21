package com.livequiz.backend.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaSubmissionRepository
  extends JpaRepository<SubmissionEntity, String> {
  interface QuestionStudentAttemptProjection {
    String getQuestionId();

    String getStudentId();

    long getAttemptCount();
  }

  Optional<SubmissionEntity> findTopByLectureIdAndQuestionIdAndStudentIdOrderBySubmittedAtDesc(
    String lectureId,
    String questionId,
    String studentId
  );

  List<SubmissionEntity> findByLectureIdAndQuestionId(
    String lectureId,
    String questionId
  );

  long countByLectureIdAndQuestionIdAndStudentId(
    String lectureId,
    String questionId,
    String studentId
  );

  @Query(
    "select distinct s.questionId from SubmissionEntity s where s.lectureId = :lectureId and s.studentId = :studentId"
  )
  List<String> findDistinctQuestionIdsByLectureAndStudent(
    @Param("lectureId") String lectureId,
    @Param("studentId") String studentId
  );

  @Query(
    """
      select s.questionId as questionId, s.studentId as studentId, count(s.id) as attemptCount
      from SubmissionEntity s
      where s.lectureId = :lectureId
      group by s.questionId, s.studentId
    """
  )
  List<QuestionStudentAttemptProjection> findQuestionStudentAttemptsByLecture(
    @Param("lectureId") String lectureId
  );
}
