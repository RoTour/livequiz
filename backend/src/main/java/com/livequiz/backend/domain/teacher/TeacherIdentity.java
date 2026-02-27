package com.livequiz.backend.domain.teacher;

import java.time.Instant;
import java.util.Locale;

public record TeacherIdentity(
  String principalId,
  boolean active,
  Instant createdAt,
  Instant updatedAt
) {
  public TeacherIdentity {
    if (principalId == null || principalId.isBlank()) {
      throw new IllegalArgumentException("Teacher principal ID cannot be null or blank");
    }
    if (createdAt == null || updatedAt == null) {
      throw new IllegalArgumentException("Teacher timestamps cannot be null");
    }
    if (updatedAt.isBefore(createdAt)) {
      throw new IllegalArgumentException("Updated date cannot be before created date");
    }

    principalId = principalId.trim().toLowerCase(Locale.ROOT);
  }

  public static TeacherIdentity active(String principalId, Instant now) {
    return new TeacherIdentity(principalId, true, now, now);
  }
}
