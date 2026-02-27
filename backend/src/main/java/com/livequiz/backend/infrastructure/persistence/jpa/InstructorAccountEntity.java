package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "instructor_accounts")
public class InstructorAccountEntity {

  @Id
  @Column(length = 320, nullable = false)
  private String email;

  @Column(length = 100, nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private boolean active;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public InstructorAccountEntity() {}

  public InstructorAccountEntity(
    String email,
    String passwordHash,
    boolean active,
    Instant createdAt,
    Instant updatedAt
  ) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.active = active;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
