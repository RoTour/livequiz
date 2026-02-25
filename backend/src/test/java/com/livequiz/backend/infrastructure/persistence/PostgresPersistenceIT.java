package com.livequiz.backend.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollment;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresPersistenceIT {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
    .withDatabaseName("livequiz")
    .withUsername("user")
    .withPassword("password");

  private static String persistedLectureId;
  private static String persistedQuestionId;
  private static String persistedInviteId;
  private static String persistedJoinCode;
  private static String persistedStudentId;
  private static Instant persistedCreatedAt;

  @Autowired
  private LectureRepository lectureRepository;

  @Autowired
  private LectureInviteRepository lectureInviteRepository;

  @Autowired
  private LectureEnrollmentRepository lectureEnrollmentRepository;

  @Autowired
  private SubmissionRepository submissionRepository;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add(
      "livequiz.jwt.secret",
      () -> "postgres-test-secret-postgres-test-secret"
    );
  }

  @Test
  @Order(1)
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void should_write_lecture_invite_enrollment_and_submission_to_postgres() {
    persistedLectureId = UUID.randomUUID().toString();
    persistedQuestionId = UUID.randomUUID().toString();
    persistedInviteId = UUID.randomUUID().toString();
    persistedJoinCode = "AB12CD";
    persistedStudentId = "student-persistence";
    persistedCreatedAt = Instant.parse("2026-02-25T10:00:00Z");

    Lecture lecture = new Lecture(
      new LectureId(persistedLectureId),
      "Postgres Persistence Lecture",
      List.of(
        new Question(
          new QuestionId(persistedQuestionId),
          "What does persistence mean?",
          "Data survives process restart",
          45,
          1,
          List.of()
        )
      ),
      Set.of(persistedQuestionId),
      "instructor-persistence",
      persistedCreatedAt
    );
    this.lectureRepository.save(lecture);

    LectureInvite invite = new LectureInvite(
      persistedInviteId,
      new LectureId(persistedLectureId),
      "instructor-persistence",
      persistedJoinCode,
      "token-hash-persistence",
      persistedCreatedAt,
      persistedCreatedAt.plus(Duration.ofHours(1)),
      null
    );
    this.lectureInviteRepository.save(invite);

    LectureEnrollment enrollment = new LectureEnrollment(
      new LectureId(persistedLectureId),
      persistedStudentId,
      persistedCreatedAt.plusSeconds(10)
    );
    this.lectureEnrollmentRepository.save(enrollment);

    Submission submission = new Submission(
      new SubmissionId(UUID.randomUUID().toString()),
      new LectureId(persistedLectureId),
      new QuestionId(persistedQuestionId),
      persistedStudentId,
      persistedCreatedAt.plusSeconds(20),
      "Durable answer"
    );
    this.submissionRepository.save(submission);

    assertTrue(this.lectureRepository.findById(new LectureId(persistedLectureId)).isPresent());
    assertTrue(this.lectureInviteRepository.findByInviteId(persistedInviteId).isPresent());
    assertTrue(
      this.lectureEnrollmentRepository
        .findByLectureIdAndStudentId(new LectureId(persistedLectureId), persistedStudentId)
        .isPresent()
    );
    assertTrue(
      this.submissionRepository
        .findLatestByLectureQuestionAndStudent(
          new LectureId(persistedLectureId),
          new QuestionId(persistedQuestionId),
          persistedStudentId
        )
        .isPresent()
    );
  }

  @Test
  @Order(2)
  void should_read_the_same_data_after_context_restart() {
    assertNotNull(persistedLectureId);
    assertNotNull(persistedQuestionId);
    assertNotNull(persistedInviteId);
    assertNotNull(persistedJoinCode);
    assertNotNull(persistedStudentId);
    assertNotNull(persistedCreatedAt);

    LectureId lectureId = new LectureId(persistedLectureId);
    QuestionId questionId = new QuestionId(persistedQuestionId);

    Lecture storedLecture = this.lectureRepository.findById(lectureId).orElseThrow();
    assertEquals("Postgres Persistence Lecture", storedLecture.title());
    assertEquals(1, storedLecture.questions().size());
    assertEquals(persistedQuestionId, storedLecture.questions().get(0).id().value());
    assertTrue(storedLecture.unlockedQuestionIds().contains(persistedQuestionId));
    assertEquals("instructor-persistence", storedLecture.createdByInstructorId());

    List<Lecture> ownedLectures = this.lectureRepository.findByCreatedByInstructorId(
        "instructor-persistence"
      );
    assertFalse(ownedLectures.isEmpty());
    assertTrue(ownedLectures.stream().anyMatch(lecture -> lecture.id().value().equals(persistedLectureId)));

    LectureInvite invite = this.lectureInviteRepository.findByInviteId(persistedInviteId).orElseThrow();
    assertEquals(persistedJoinCode, invite.joinCode());
    assertEquals(persistedLectureId, invite.lectureId().value());

    assertTrue(
      this.lectureInviteRepository
        .findByLectureId(lectureId)
        .stream()
        .anyMatch(foundInvite -> foundInvite.inviteId().equals(persistedInviteId))
    );
    assertTrue(
      this.lectureInviteRepository
        .findActiveByJoinCode(persistedJoinCode, persistedCreatedAt.plus(Duration.ofMinutes(30)))
        .isPresent()
    );
    assertTrue(
      this.lectureInviteRepository
        .existsActiveByJoinCode(
        persistedJoinCode,
        persistedCreatedAt.plus(Duration.ofMinutes(30))
      )
    );

    assertEquals(1, this.lectureEnrollmentRepository.countByLectureId(lectureId));
    assertTrue(
      this.lectureEnrollmentRepository
        .findStudentIdsByLectureId(lectureId)
        .contains(persistedStudentId)
    );
    assertTrue(
      this.lectureEnrollmentRepository
        .findByStudentId(persistedStudentId)
        .stream()
        .anyMatch(enrollment -> enrollment.lectureId().value().equals(persistedLectureId))
    );
    assertTrue(
      this.lectureEnrollmentRepository
        .existsByLectureIdAndStudentId(
          lectureId,
          persistedStudentId
        )
    );

    Submission latestSubmission = this.submissionRepository
      .findLatestByLectureQuestionAndStudent(lectureId, questionId, persistedStudentId)
      .orElseThrow();
    assertEquals("Durable answer", latestSubmission.answerText());
    assertEquals(
      1,
      this.submissionRepository.countByLectureQuestionAndStudent(
        lectureId,
        questionId,
        persistedStudentId
      )
    );
    assertTrue(
      this.submissionRepository
        .findSubmittedQuestionIdsByLectureAndStudent(lectureId, persistedStudentId)
        .contains(persistedQuestionId)
    );
    assertTrue(
      this.submissionRepository
        .findQuestionStudentAttemptsByLecture(lectureId)
        .stream()
        .anyMatch(attempt ->
          attempt.questionId().equals(persistedQuestionId) &&
          attempt.studentId().equals(persistedStudentId) &&
          attempt.attemptCount() == 1
        )
    );
  }
}
