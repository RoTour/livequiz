package com.livequiz.backend.domain.instructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstructorAccountTest {

  @Test
  void should_normalize_email_to_lowercase() {
    Instant now = Instant.now();

    InstructorAccount account = new InstructorAccount(
      " Instructor@Ynov.com ",
      "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO1234567890123",
      true,
      now,
      now
    );

    assertEquals("instructor@ynov.com", account.email());
  }

  @Test
  void should_reject_invalid_email() {
    Instant now = Instant.now();

    assertThrows(
      IllegalArgumentException.class,
      () ->
        new InstructorAccount(
          "invalid-email",
          "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO1234567890123",
          true,
          now,
          now
        )
    );
  }
}
