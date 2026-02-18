package com.livequiz.backend.domain.lecture;

import java.time.Instant;

public record LectureEnrollment(LectureId lectureId, String studentId, Instant enrolledAt) {
  public LectureEnrollment {
    if (lectureId == null) {
      throw new IllegalArgumentException("Lecture ID cannot be null");
    }
    if (studentId == null || studentId.isBlank()) {
      throw new IllegalArgumentException("Student ID cannot be null or blank");
    }
    if (enrolledAt == null) {
      throw new IllegalArgumentException("Enrollment date cannot be null");
    }
  }
}
