package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_identities")
public class StudentIdentityEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String studentId;

  @Column(length = 320)
  private String email;

  @Column(length = 64, nullable = false)
  private String status;

  private Instant emailVerifiedAt;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public StudentIdentityEntity() {}

  public StudentIdentityEntity(
    String studentId,
    String email,
    String status,
    Instant emailVerifiedAt,
    Instant createdAt,
    Instant updatedAt
  ) {
    this.studentId = studentId;
    this.email = email;
    this.status = status;
    this.emailVerifiedAt = emailVerifiedAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getStudentId() {
    return studentId;
  }

  public String getEmail() {
    return email;
  }

  public String getStatus() {
    return status;
  }

  public Instant getEmailVerifiedAt() {
    return emailVerifiedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
