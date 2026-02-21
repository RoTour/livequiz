package com.livequiz.backend.domain.lecture;

public interface LectureEnrollmentRepository {
  void save(LectureEnrollment enrollment);

  long countByLectureId(LectureId lectureId);

  boolean existsByLectureIdAndStudentId(LectureId lectureId, String studentId);

  java.util.Optional<LectureEnrollment> findByLectureIdAndStudentId(
    LectureId lectureId,
    String studentId
  );
}
