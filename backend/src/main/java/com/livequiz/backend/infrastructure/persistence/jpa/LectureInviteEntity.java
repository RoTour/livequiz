package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "lecture_invites")
public class LectureInviteEntity {

  @Id
  private String id;

  private String lectureId;
  private String createdByInstructorId;
  private String joinCode;
  private String tokenHash;
  private Instant createdAt;
  private Instant expiresAt;
  private Instant revokedAt;

  public LectureInviteEntity() {}

  public LectureInviteEntity(
    String id,
    String lectureId,
    String createdByInstructorId,
    String joinCode,
    String tokenHash,
    Instant createdAt,
    Instant expiresAt,
    Instant revokedAt
  ) {
    this.id = id;
    this.lectureId = lectureId;
    this.createdByInstructorId = createdByInstructorId;
    this.joinCode = joinCode;
    this.tokenHash = tokenHash;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.revokedAt = revokedAt;
  }

  public String getId() {
    return id;
  }

  public String getLectureId() {
    return lectureId;
  }

  public String getCreatedByInstructorId() {
    return createdByInstructorId;
  }

  public String getJoinCode() {
    return joinCode;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }
}
