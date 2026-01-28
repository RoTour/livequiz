package com.livequiz.backend.domain.quiz;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class QuizTest {

  @Test
  void quiz_creation_should_fail_if_empty_title_provided() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Quiz(new QuizId("quiz-123"), "");
      },
      "Title cannot be null or blank"
    );
  }
}
