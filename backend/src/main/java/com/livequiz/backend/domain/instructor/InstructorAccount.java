package com.livequiz.backend.domain.instructor;

import java.time.Instant;
import java.util.Locale;

public record InstructorAccount(
  String email,
  String passwordHash,
  boolean active,
  Instant createdAt,
  Instant updatedAt
) {
  public InstructorAccount {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Instructor email cannot be null or blank");
    }
    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    if (!normalizedEmail.contains("@")) {
      throw new IllegalArgumentException("Instructor email must be a valid email address");
    }

    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Instructor password hash cannot be null or blank");
    }

    if (createdAt == null || updatedAt == null) {
      throw new IllegalArgumentException("Instructor account timestamps cannot be null");
    }

    if (updatedAt.isBefore(createdAt)) {
      throw new IllegalArgumentException("Updated date cannot be before created date");
    }

    email = normalizedEmail;
  }
}
