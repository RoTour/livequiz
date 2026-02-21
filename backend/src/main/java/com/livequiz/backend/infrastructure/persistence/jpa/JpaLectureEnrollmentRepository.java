package com.livequiz.backend.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLectureEnrollmentRepository
  extends JpaRepository<LectureEnrollmentEntity, LectureEnrollmentId> {
  long countByIdLectureId(String lectureId);

  java.util.List<LectureEnrollmentEntity> findByIdLectureId(String lectureId);
}
