package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lectures")
public class LectureEntity {

  @Id
  private String id;

  private String title;

  private String createdByInstructorId;

  private Instant createdAt;

  @ElementCollection
  @CollectionTable(
    name = "lecture_questions",
    joinColumns = @JoinColumn(name = "lecture_id")
  )
  private List<LectureQuestionEmbeddable> questions = new ArrayList<>();

  @ElementCollection
  @CollectionTable(
    name = "lecture_unlocked_questions",
    joinColumns = @JoinColumn(name = "lecture_id")
  )
  @Column(name = "question_id")
  private List<String> unlockedQuestionIds = new ArrayList<>();

  public LectureEntity() {}

  public LectureEntity(String id, String title) {
    this(id, title, new ArrayList<>(), new ArrayList<>(), null, null);
  }

  public LectureEntity(
    String id,
    String title,
    List<LectureQuestionEmbeddable> questions,
    List<String> unlockedQuestionIds
  ) {
    this(id, title, questions, unlockedQuestionIds, null, null);
  }

  public LectureEntity(
    String id,
    String title,
    List<LectureQuestionEmbeddable> questions,
    List<String> unlockedQuestionIds,
    String createdByInstructorId,
    Instant createdAt
  ) {
    this.id = id;
    this.title = title;
    this.questions = questions;
    this.unlockedQuestionIds = unlockedQuestionIds;
    this.createdByInstructorId = createdByInstructorId;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public List<LectureQuestionEmbeddable> getQuestions() {
    return questions;
  }

  public List<String> getUnlockedQuestionIds() {
    return unlockedQuestionIds;
  }

  public String getCreatedByInstructorId() {
    return createdByInstructorId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
