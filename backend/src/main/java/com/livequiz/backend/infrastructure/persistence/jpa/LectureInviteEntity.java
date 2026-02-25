package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "lecture_invites")
public class LectureInviteEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String id;

  @Column(length = 255, nullable = false)
  private String lectureId;

  @Column(length = 255, nullable = false)
  private String createdByInstructorId;

  @Column(length = 16, nullable = false)
  private String joinCode;

  @Column(length = 128, nullable = false)
  private String tokenHash;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
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
