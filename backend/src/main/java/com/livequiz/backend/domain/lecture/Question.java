package com.livequiz.backend.domain.lecture;

import java.util.List;

public class Question {

  private final QuestionId id;
  private final String prompt;
  private final String modelAnswer;
  private final int timeLimitSeconds;
  private final int order;
  private final List<KeyPoint> keyPoints;

  public Question(
    QuestionId id,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds,
    int order,
    List<KeyPoint> keyPoints
  ) {
    if (id == null) {
      throw new IllegalArgumentException("Question ID cannot be null");
    }
    if (prompt == null || prompt.isBlank()) {
      throw new IllegalArgumentException("Prompt cannot be null or blank");
    }
    if (modelAnswer == null || modelAnswer.isBlank()) {
      throw new IllegalArgumentException("Model answer cannot be null or blank");
    }
    if (timeLimitSeconds <= 0) {
      throw new IllegalArgumentException("Time limit must be positive");
    }
    if (order <= 0) {
      throw new IllegalArgumentException("Question order must be positive");
    }
    this.id = id;
    this.prompt = prompt;
    this.modelAnswer = modelAnswer;
    this.timeLimitSeconds = timeLimitSeconds;
    this.order = order;
    this.keyPoints = keyPoints != null ? List.copyOf(keyPoints) : List.of();
  }

  public QuestionId id() {
    return id;
  }

  public String prompt() {
    return prompt;
  }

  public String modelAnswer() {
    return modelAnswer;
  }

  public int timeLimitSeconds() {
    return timeLimitSeconds;
  }

  public int order() {
    return order;
  }

  public List<KeyPoint> keyPoints() {
    return keyPoints;
  }
}
