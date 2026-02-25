package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class LectureQuestionEmbeddable {

  @Column(name = "question_id", length = 255, nullable = false)
  private String questionId;

  @Column(columnDefinition = "text", nullable = false)
  private String prompt;

  @Column(name = "model_answer", columnDefinition = "text", nullable = false)
  private String modelAnswer;

  @Column(name = "time_limit_seconds", nullable = false)
  private int timeLimitSeconds;

  @Column(name = "question_order", nullable = false)
  private int questionOrder;

  public LectureQuestionEmbeddable() {}

  public LectureQuestionEmbeddable(
    String questionId,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds,
    int questionOrder
  ) {
    this.questionId = questionId;
    this.prompt = prompt;
    this.modelAnswer = modelAnswer;
    this.timeLimitSeconds = timeLimitSeconds;
    this.questionOrder = questionOrder;
  }

  public String getQuestionId() {
    return questionId;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getModelAnswer() {
    return modelAnswer;
  }

  public int getTimeLimitSeconds() {
    return timeLimitSeconds;
  }

  public int getQuestionOrder() {
    return questionOrder;
  }
}
