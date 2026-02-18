package com.livequiz.backend.domain.lecture;

public record QuestionId(String value) {
  public QuestionId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Question ID cannot be empty");
    }
  }
}
