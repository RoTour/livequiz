package com.livequiz.backend.domain.lecture;

public interface LectureEnrollmentRepository {
  void save(LectureEnrollment enrollment);

  long countByLectureId(LectureId lectureId);

  java.util.List<String> findStudentIdsByLectureId(LectureId lectureId);

  java.util.List<LectureEnrollment> findByStudentId(String studentId);

  boolean existsByLectureIdAndStudentId(LectureId lectureId, String studentId);

  java.util.Optional<LectureEnrollment> findByLectureIdAndStudentId(
    LectureId lectureId,
    String studentId
  );
}
