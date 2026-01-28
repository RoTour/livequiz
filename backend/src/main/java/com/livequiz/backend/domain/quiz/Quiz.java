package com.livequiz.backend.domain.quiz;

public class Quiz {

  private final QuizId id;
  private final String title;

  public Quiz(QuizId id, String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or blank");
    }
    this.id = id;
    this.title = title;
  }

  public QuizId id() {
    return id;
  }

  public String title() {
    return title;
  }
}
