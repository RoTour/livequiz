package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_verification_challenges")
public class EmailVerificationChallengeEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String challengeId;

  @Column(length = 255, nullable = false)
  private String studentId;

  @Column(length = 320, nullable = false)
  private String email;

  @Column(length = 128, nullable = false)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant consumedAt;

  @Column(nullable = false)
  private Instant createdAt;

  public EmailVerificationChallengeEntity() {}

  public EmailVerificationChallengeEntity(
    String challengeId,
    String studentId,
    String email,
    String tokenHash,
    Instant expiresAt,
    Instant consumedAt,
    Instant createdAt
  ) {
    this.challengeId = challengeId;
    this.studentId = studentId;
    this.email = email;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.consumedAt = consumedAt;
    this.createdAt = createdAt;
  }

  public String getChallengeId() {
    return challengeId;
  }

  public String getStudentId() {
    return studentId;
  }

  public String getEmail() {
    return email;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getConsumedAt() {
    return consumedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
