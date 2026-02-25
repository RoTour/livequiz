package com.livequiz.backend.domain.student;

import java.time.Instant;

public record EmailVerificationChallenge(
  String challengeId,
  String studentId,
  String email,
  String tokenHash,
  Instant expiresAt,
  Instant consumedAt,
  Instant createdAt
) {
  public EmailVerificationChallenge {
    if (challengeId == null || challengeId.isBlank()) {
      throw new IllegalArgumentException("Challenge ID cannot be blank");
    }
    if (studentId == null || studentId.isBlank()) {
      throw new IllegalArgumentException("Student ID cannot be blank");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Challenge email cannot be blank");
    }
    if (tokenHash == null || tokenHash.isBlank()) {
      throw new IllegalArgumentException("Challenge token hash cannot be blank");
    }
    if (createdAt == null || expiresAt == null) {
      throw new IllegalArgumentException("Challenge dates cannot be null");
    }
    if (!createdAt.isBefore(expiresAt)) {
      throw new IllegalArgumentException("Challenge expiration must be after creation");
    }
    if (consumedAt != null && consumedAt.isBefore(createdAt)) {
      throw new IllegalArgumentException("Challenge consumed date cannot be before creation");
    }
  }

  public boolean isExpiredAt(Instant now) {
    return !now.isBefore(this.expiresAt);
  }

  public boolean isConsumed() {
    return this.consumedAt != null;
  }

  public EmailVerificationChallenge consume(Instant consumedAt) {
    if (consumedAt == null) {
      throw new IllegalArgumentException("Consumed date cannot be null");
    }
    if (this.consumedAt != null) {
      throw new IllegalArgumentException("Challenge is already consumed");
    }
    return new EmailVerificationChallenge(
      this.challengeId,
      this.studentId,
      this.email,
      this.tokenHash,
      this.expiresAt,
      consumedAt,
      this.createdAt
    );
  }
}
