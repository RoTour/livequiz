package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.lecture.LectureEnrollment;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryLectureEnrollmentRepository
  implements LectureEnrollmentRepository {

  private final ConcurrentMap<String, LectureEnrollment> enrollments = new ConcurrentHashMap<>();

  @Override
  public void save(LectureEnrollment enrollment) {
    this.enrollments.put(key(enrollment.lectureId(), enrollment.studentId()), enrollment);
  }

  @Override
  public long countByLectureId(LectureId lectureId) {
    return this.enrollments
      .values()
      .stream()
      .filter(enrollment -> enrollment.lectureId().value().equals(lectureId.value()))
      .count();
  }

  @Override
  public java.util.List<String> findStudentIdsByLectureId(LectureId lectureId) {
    return this.enrollments
      .values()
      .stream()
      .filter(enrollment -> enrollment.lectureId().value().equals(lectureId.value()))
      .map(LectureEnrollment::studentId)
      .toList();
  }

  @Override
  public java.util.List<LectureEnrollment> findByStudentId(String studentId) {
    return this.enrollments
      .values()
      .stream()
      .filter(enrollment -> enrollment.studentId().equals(studentId))
      .sorted(java.util.Comparator.comparing(LectureEnrollment::enrolledAt).reversed())
      .toList();
  }

  @Override
  public boolean existsByLectureIdAndStudentId(LectureId lectureId, String studentId) {
    return this.enrollments.containsKey(key(lectureId, studentId));
  }

  @Override
  public Optional<LectureEnrollment> findByLectureIdAndStudentId(
    LectureId lectureId,
    String studentId
  ) {
    return Optional.ofNullable(this.enrollments.get(key(lectureId, studentId)));
  }

  private String key(LectureId lectureId, String studentId) {
    return lectureId.value() + "::" + studentId;
  }
}
