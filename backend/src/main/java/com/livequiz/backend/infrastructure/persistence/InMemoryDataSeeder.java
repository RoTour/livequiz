package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.lecture.QuestionId;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "in-memory", "memory" })
public class InMemoryDataSeeder {

  private static final String DEFAULT_LECTURE_ID = "demo-lecture";

  private final InMemoryLectureRepository lectureRepository;

  public InMemoryDataSeeder(InMemoryLectureRepository lectureRepository) {
    this.lectureRepository = lectureRepository;
  }

  @PostConstruct
  public void seedDefaultLecture() {
    boolean alreadySeeded = this.lectureRepository.findById(new LectureId(DEFAULT_LECTURE_ID)).isPresent();
    if (alreadySeeded) {
      return;
    }

    Question defaultQuestion = new Question(
      new QuestionId("demo-question-1"),
      "In one sentence, define what an aggregate root does.",
      "An aggregate root enforces invariants and is the only entry point to change aggregate state.",
      90,
      1,
      List.of()
    );

    Lecture defaultLecture = new Lecture(
      new LectureId(DEFAULT_LECTURE_ID),
      "DDD Warm-up Lecture",
      List.of(defaultQuestion),
      Set.of(defaultQuestion.id().value()),
      "instructor",
      Instant.now()
    );
    this.lectureRepository.save(defaultLecture);
  }
}
