package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "lecture_enrollments")
public class LectureEnrollmentEntity {

  @EmbeddedId
  private LectureEnrollmentId id;

  private Instant enrolledAt;

  public LectureEnrollmentEntity() {}

  public LectureEnrollmentEntity(LectureEnrollmentId id, Instant enrolledAt) {
    this.id = id;
    this.enrolledAt = enrolledAt;
  }

  public LectureEnrollmentId getId() {
    return id;
  }

  public Instant getEnrolledAt() {
    return enrolledAt;
  }
}
