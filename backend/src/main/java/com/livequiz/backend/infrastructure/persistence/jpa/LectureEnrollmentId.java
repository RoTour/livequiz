package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LectureEnrollmentId implements Serializable {

  private String lectureId;
  private String studentId;

  public LectureEnrollmentId() {}

  public LectureEnrollmentId(String lectureId, String studentId) {
    this.lectureId = lectureId;
    this.studentId = studentId;
  }

  public String getLectureId() {
    return lectureId;
  }

  public String getStudentId() {
    return studentId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LectureEnrollmentId that)) {
      return false;
    }
    return Objects.equals(lectureId, that.lectureId) && Objects.equals(studentId, that.studentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lectureId, studentId);
  }
}
