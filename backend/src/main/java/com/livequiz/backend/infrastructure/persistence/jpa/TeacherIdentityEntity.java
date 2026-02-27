package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "teacher_identities")
public class TeacherIdentityEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String principalId;

  @Column(nullable = false)
  private boolean active;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public TeacherIdentityEntity() {}

  public TeacherIdentityEntity(
    String principalId,
    boolean active,
    Instant createdAt,
    Instant updatedAt
  ) {
    this.principalId = principalId;
    this.active = active;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getPrincipalId() {
    return principalId;
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
