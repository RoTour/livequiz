package com.livequiz.backend.domain.lecture;

import java.time.Duration;
import java.time.Instant;

public record LectureInvite(
  String inviteId,
  LectureId lectureId,
  String createdByInstructorId,
  String joinCode,
  String tokenHash,
  Instant createdAt,
  Instant expiresAt,
  Instant revokedAt
) {
  public static final Duration MAX_TTL = Duration.ofDays(1);

  public LectureInvite {
    if (inviteId == null || inviteId.isBlank()) {
      throw new IllegalArgumentException("Invite ID cannot be empty");
    }
    if (lectureId == null) {
      throw new IllegalArgumentException("Lecture ID cannot be null");
    }
    if (createdByInstructorId == null || createdByInstructorId.isBlank()) {
      throw new IllegalArgumentException("Instructor ID cannot be empty");
    }
    if (joinCode == null || joinCode.isBlank()) {
      throw new IllegalArgumentException("Join code cannot be empty");
    }
    if (tokenHash == null || tokenHash.isBlank()) {
      throw new IllegalArgumentException("Token hash cannot be empty");
    }
    if (createdAt == null || expiresAt == null) {
      throw new IllegalArgumentException("Invite dates cannot be null");
    }
    Duration ttl = Duration.between(createdAt, expiresAt);
    if (ttl.isNegative() || ttl.isZero()) {
      throw new IllegalArgumentException("Invite expiration must be in the future");
    }
    if (ttl.compareTo(MAX_TTL) > 0) {
      throw new IllegalArgumentException("Invite expiration cannot exceed 24 hours");
    }
  }

  public boolean isActiveAt(Instant now) {
    return revokedAt == null && now.isBefore(expiresAt);
  }

  public LectureInvite revoke(Instant revokedAt) {
    if (revokedAt == null) {
      throw new IllegalArgumentException("Revoke date cannot be null");
    }
    return new LectureInvite(
      inviteId,
      lectureId,
      createdByInstructorId,
      joinCode,
      tokenHash,
      createdAt,
      expiresAt,
      revokedAt
    );
  }
}
