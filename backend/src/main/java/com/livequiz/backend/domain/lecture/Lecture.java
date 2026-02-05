package com.livequiz.backend.domain.lecture;

public class Lecture {

  private final LectureId id;
  private final String title;

  public Lecture(LectureId id, String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or blank");
    }
    this.id = id;
    this.title = title;
  }

  public LectureId id() {
    return id;
  }

  public String title() {
    return title;
  }
}
