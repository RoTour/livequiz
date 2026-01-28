package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quizzes")
public class QuizEntity {

  @Id
  private String id;

  private String title;

  public QuizEntity() {}

  public QuizEntity(String id, String title) {
    this.id = id;
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }
}
