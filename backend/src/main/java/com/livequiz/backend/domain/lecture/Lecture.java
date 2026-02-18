package com.livequiz.backend.domain.lecture;

public class Lecture {

  private final LectureId id;
  private final String title;
  private final java.util.List<Question> questions;
  private final java.util.Set<String> unlockedQuestionIds;

  public Lecture(LectureId id, String title) {
    this(id, title, java.util.List.of(), java.util.Set.of());
  }

  public Lecture(LectureId id, String title, java.util.List<Question> questions) {
    this(id, title, questions, java.util.Set.of());
  }

  public Lecture(
    LectureId id,
    String title,
    java.util.List<Question> questions,
    java.util.Set<String> unlockedQuestionIds
  ) {
    if (id == null) {
      throw new IllegalArgumentException("Lecture ID cannot be null");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or blank");
    }
    this.id = id;
    this.title = title;
    this.questions = questions != null ? java.util.List.copyOf(questions) : java.util.List.of();
    this.unlockedQuestionIds =
      unlockedQuestionIds != null
        ? java.util.Set.copyOf(unlockedQuestionIds)
        : java.util.Set.of();

    long distinctQuestionCount = this.questions.stream().map(q -> q.id().value()).distinct().count();
    if (distinctQuestionCount != this.questions.size()) {
      throw new IllegalArgumentException("Question IDs must be unique within a lecture");
    }

    boolean hasUnknownUnlockedQuestions = this.unlockedQuestionIds
      .stream()
      .anyMatch(idValue -> this.questions.stream().noneMatch(q -> q.id().value().equals(idValue)));
    if (hasUnknownUnlockedQuestions) {
      throw new IllegalArgumentException("Unlocked questions must exist in lecture questions");
    }
  }

  public LectureId id() {
    return id;
  }

  public String title() {
    return title;
  }

  public java.util.List<Question> questions() {
    return questions;
  }

  public java.util.Set<String> unlockedQuestionIds() {
    return unlockedQuestionIds;
  }

  public Lecture addQuestion(String questionId, String prompt, String modelAnswer, int timeLimitSeconds) {
    if (this.questions.stream().anyMatch(q -> q.id().value().equals(questionId))) {
      throw new IllegalArgumentException("Question already exists in lecture");
    }

    int nextOrder = this.questions.stream().mapToInt(Question::order).max().orElse(0) + 1;
    Question question = new Question(
      new QuestionId(questionId),
      prompt,
      modelAnswer,
      timeLimitSeconds,
      nextOrder,
      java.util.List.of()
    );

    java.util.List<Question> updatedQuestions = new java.util.ArrayList<>(this.questions);
    updatedQuestions.add(question);
    return new Lecture(this.id, this.title, updatedQuestions, this.unlockedQuestionIds);
  }

  public Lecture unlockQuestion(String questionId) {
    if (this.questions.stream().noneMatch(q -> q.id().value().equals(questionId))) {
      throw new IllegalArgumentException("Question not found in lecture");
    }

    java.util.Set<String> updatedUnlockedQuestions = new java.util.LinkedHashSet<>(
      this.unlockedQuestionIds
    );
    updatedUnlockedQuestions.add(questionId);
    return new Lecture(this.id, this.title, this.questions, updatedUnlockedQuestions);
  }

  public Lecture unlockNextQuestion() {
    java.util.Optional<Question> nextQuestion = this.questions
      .stream()
      .sorted(java.util.Comparator.comparingInt(Question::order))
      .filter(question -> !this.unlockedQuestionIds.contains(question.id().value()))
      .findFirst();

    if (nextQuestion.isEmpty()) {
      return this;
    }

    return this.unlockQuestion(nextQuestion.get().id().value());
  }
}
