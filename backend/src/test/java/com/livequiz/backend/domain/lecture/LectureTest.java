package com.livequiz.backend.domain.lecture;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LectureTest {

  @Test
  void lecture_creation_should_fail_if_empty_title_provided() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Lecture(new LectureId("lecture-123"), "");
      },
      "Title cannot be null or blank"
    );
  }

  @Test
  void should_add_question_and_unlock_next_in_order() {
    Lecture lecture = new Lecture(new LectureId("lecture-123"), "Physics 101");
    Lecture withQuestionOne = lecture.addQuestion(
      "q-1",
      "What is inertia?",
      "Resistance to change in motion",
      60
    );
    Lecture withQuestionTwo = withQuestionOne.addQuestion(
      "q-2",
      "What is force?",
      "Mass times acceleration",
      60
    );

    Lecture firstUnlock = withQuestionTwo.unlockNextQuestion();
    assertTrue(firstUnlock.unlockedQuestionIds().contains("q-1"));
    assertFalse(firstUnlock.unlockedQuestionIds().contains("q-2"));

    Lecture secondUnlock = firstUnlock.unlockNextQuestion();
    assertTrue(secondUnlock.unlockedQuestionIds().contains("q-1"));
    assertTrue(secondUnlock.unlockedQuestionIds().contains("q-2"));
  }

  @Test
  void should_reject_duplicate_question_ids() {
    Lecture lecture = new Lecture(new LectureId("lecture-123"), "Physics 101");
    Lecture withQuestion = lecture.addQuestion(
      "q-1",
      "What is inertia?",
      "Resistance to change in motion",
      60
    );

    assertThrows(
      IllegalArgumentException.class,
      () ->
        withQuestion.addQuestion(
          "q-1",
          "What is acceleration?",
          "Rate of velocity change",
          60
        )
    );
  }

  @Test
  void should_preserve_ownership_metadata_across_mutations() {
    var createdAt = java.time.Instant.parse("2026-02-21T11:00:00Z");
    Lecture lecture = new Lecture(
      new LectureId("lecture-123"),
      "Physics 101",
      "instructor-1",
      createdAt
    );

    Lecture withQuestion = lecture.addQuestion(
      "q-1",
      "What is inertia?",
      "Resistance to change in motion",
      60
    );
    Lecture unlocked = withQuestion.unlockQuestion("q-1");

    assertEquals("instructor-1", unlocked.createdByInstructorId());
    assertEquals(createdAt, unlocked.createdAt());
  }
}
