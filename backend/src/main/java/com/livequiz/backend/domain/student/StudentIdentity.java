package com.livequiz.backend.domain.student;

import java.time.Instant;
import java.util.Locale;

public record StudentIdentity(
  String studentId,
  String email,
  StudentIdentityStatus status,
  Instant emailVerifiedAt,
  Instant createdAt,
  Instant updatedAt
) {
  public StudentIdentity {
    if (studentId == null || studentId.isBlank()) {
      throw new IllegalArgumentException("Student ID cannot be null or blank");
    }
    if (status == null) {
      throw new IllegalArgumentException("Student status cannot be null");
    }
    if (createdAt == null || updatedAt == null) {
      throw new IllegalArgumentException("Student timestamps cannot be null");
    }
    if (updatedAt.isBefore(createdAt)) {
      throw new IllegalArgumentException("Updated date cannot be before created date");
    }

    String normalizedEmail = email == null
      ? null
      : email.trim().toLowerCase(Locale.ROOT);
    email = normalizedEmail == null || normalizedEmail.isBlank() ? null : normalizedEmail;

    if (status == StudentIdentityStatus.ANONYMOUS) {
      if (email != null) {
        throw new IllegalArgumentException("Anonymous student cannot have an email");
      }
      if (emailVerifiedAt != null) {
        throw new IllegalArgumentException("Anonymous student cannot have verified timestamp");
      }
    }

    if (status == StudentIdentityStatus.REGISTERED_UNVERIFIED) {
      if (email == null) {
        throw new IllegalArgumentException("Registered unverified student must have an email");
      }
      if (emailVerifiedAt != null) {
        throw new IllegalArgumentException("Unverified student cannot have verified timestamp");
      }
    }

    if (status == StudentIdentityStatus.REGISTERED_VERIFIED) {
      if (email == null) {
        throw new IllegalArgumentException("Verified student must have an email");
      }
      if (emailVerifiedAt == null) {
        throw new IllegalArgumentException("Verified student must have verified timestamp");
      }
    }
  }

  public static StudentIdentity anonymous(String studentId, Instant now) {
    return new StudentIdentity(
      studentId,
      null,
      StudentIdentityStatus.ANONYMOUS,
      null,
      now,
      now
    );
  }

  public StudentIdentity registerEmail(String normalizedEmail, Instant now) {
    if (normalizedEmail == null || normalizedEmail.isBlank()) {
      throw new IllegalArgumentException("Email cannot be null or blank");
    }
    if (now == null) {
      throw new IllegalArgumentException("Updated date cannot be null");
    }
    return new StudentIdentity(
      this.studentId,
      normalizedEmail,
      StudentIdentityStatus.REGISTERED_UNVERIFIED,
      null,
      this.createdAt,
      now
    );
  }

  public StudentIdentity verifyEmail(Instant now) {
    if (now == null) {
      throw new IllegalArgumentException("Verification date cannot be null");
    }
    if (this.email == null || this.email.isBlank()) {
      throw new IllegalArgumentException("Student email cannot be blank when verifying");
    }
    return new StudentIdentity(
      this.studentId,
      this.email,
      StudentIdentityStatus.REGISTERED_VERIFIED,
      now,
      this.createdAt,
      now
    );
  }
}
