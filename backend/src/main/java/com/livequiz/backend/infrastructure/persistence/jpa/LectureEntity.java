package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "lectures")
public class LectureEntity {

  @Id
  private String id;

  @Column(length = 1000, nullable = false)
  private String title;

  @Column(length = 255)
  private String createdByInstructorId;

  private Instant createdAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "lecture_questions",
    joinColumns = @JoinColumn(name = "lecture_id")
  )
  private List<LectureQuestionEmbeddable> questions = new ArrayList<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "lecture_unlocked_questions",
    joinColumns = @JoinColumn(name = "lecture_id")
  )
  @Column(name = "question_id", length = 255, nullable = false)
  private Set<String> unlockedQuestionIds = new LinkedHashSet<>();

  public LectureEntity() {}

  public LectureEntity(String id, String title) {
    this(id, title, new ArrayList<>(), new LinkedHashSet<>(), null, null);
  }

  public LectureEntity(
    String id,
    String title,
    List<LectureQuestionEmbeddable> questions,
    Set<String> unlockedQuestionIds
  ) {
    this(id, title, questions, unlockedQuestionIds, null, null);
  }

  public LectureEntity(
    String id,
    String title,
    List<LectureQuestionEmbeddable> questions,
    Set<String> unlockedQuestionIds,
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

  public Set<String> getUnlockedQuestionIds() {
    return unlockedQuestionIds;
  }

  public String getCreatedByInstructorId() {
    return createdByInstructorId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
