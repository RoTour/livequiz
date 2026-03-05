package com.livequiz.backend.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureEnrollment;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.Question;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.domain.student.StudentIdentityStatus;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionId;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GetQuestionStudentAnswerHistoryUseCaseTest {

  @Test
  void should_project_verified_email_latest_submission_and_zero_attempt_students() {
    Instant now = Instant.parse("2026-03-01T10:00:00Z");
    Lecture lecture = lectureWithQuestion("lecture-1", "question-1");
    GetQuestionStudentAnswerHistoryUseCase useCase = new GetQuestionStudentAnswerHistoryUseCase(
      new FixedInstructorLectureAccessService(lecture),
      new InMemoryLectureEnrollmentRepository(List.of("student-a", "student-b", "student-c")),
      new InMemoryStudentIdentityRepository(
        List.of(
          new StudentIdentity(
            "student-a",
            "student.verified@example.com",
            StudentIdentityStatus.REGISTERED_VERIFIED,
            now,
            now,
            now
          ),
          new StudentIdentity(
            "student-b",
            "student.unverified@example.com",
            StudentIdentityStatus.REGISTERED_UNVERIFIED,
            null,
            now,
            now
          )
        )
      ),
      new InMemorySubmissionRepository(
        List.of(
          submission(
            "submission-1",
            "lecture-1",
            "question-1",
            "student-a",
            now.plusSeconds(20),
            "latest answer"
          ),
          submission(
            "submission-2",
            "lecture-1",
            "question-1",
            "student-a",
            now.plusSeconds(10),
            "first answer"
          ),
          submission(
            "submission-3",
            "lecture-1",
            "question-1",
            "student-b",
            now.plusSeconds(15),
            "single answer"
          )
        )
      )
    );

    List<GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory> result = useCase.execute(
      "lecture-1",
      "question-1"
    );

    assertEquals(
      List.of("student-a", "student-b", "student-c"),
      result.stream().map(GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory::studentId).toList()
    );

    Map<String, GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory> historiesByStudent = result
      .stream()
      .collect(
        Collectors.toMap(GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory::studentId, history -> history)
      );

    GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory studentAHistory = historiesByStudent.get(
      "student-a"
    );
    assertEquals("student.verified@example.com", studentAHistory.studentEmail());
    assertEquals(2, studentAHistory.attemptCount());
    assertEquals(now.plusSeconds(20), studentAHistory.latestAnswerAt());
    assertEquals("latest answer", studentAHistory.latestAnswerText());

    GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory studentBHistory = historiesByStudent.get(
      "student-b"
    );
    assertNull(studentBHistory.studentEmail());
    assertEquals(1, studentBHistory.attemptCount());
    assertEquals(now.plusSeconds(15), studentBHistory.latestAnswerAt());
    assertEquals("single answer", studentBHistory.latestAnswerText());

    GetQuestionStudentAnswerHistoryUseCase.StudentAnswerHistory studentCHistory = historiesByStudent.get(
      "student-c"
    );
    assertNull(studentCHistory.studentEmail());
    assertEquals(0, studentCHistory.attemptCount());
    assertNull(studentCHistory.latestAnswerAt());
    assertNull(studentCHistory.latestAnswerText());
  }

  @Test
  void should_throw_not_found_when_question_is_missing_from_lecture() {
    Lecture lecture = lectureWithQuestion("lecture-1", "question-1");
    GetQuestionStudentAnswerHistoryUseCase useCase = new GetQuestionStudentAnswerHistoryUseCase(
      new FixedInstructorLectureAccessService(lecture),
      new InMemoryLectureEnrollmentRepository(List.of("student-a")),
      new InMemoryStudentIdentityRepository(List.of()),
      new InMemorySubmissionRepository(List.of())
    );

    ApiException exception = assertThrows(ApiException.class, () ->
      useCase.execute("lecture-1", "question-unknown")
    );

    assertEquals("QUESTION_NOT_FOUND", exception.code());
  }

  private static Lecture lectureWithQuestion(String lectureId, String questionId) {
    return new Lecture(
      new LectureId(lectureId),
      "History lecture",
      List.of(new Question(new QuestionId(questionId), "Prompt", "Model answer", 60, 1, List.of())),
      Set.of(),
      "instructor-1",
      Instant.parse("2026-03-01T09:00:00Z")
    );
  }

  private static Submission submission(
    String submissionId,
    String lectureId,
    String questionId,
    String studentId,
    Instant timestamp,
    String answerText
  ) {
    return new Submission(
      new SubmissionId(submissionId),
      new LectureId(lectureId),
      new QuestionId(questionId),
      studentId,
      timestamp,
      answerText
    );
  }

  private static class FixedInstructorLectureAccessService extends InstructorLectureAccessService {

    private final Lecture lecture;

    private FixedInstructorLectureAccessService(Lecture lecture) {
      super(null, null);
      this.lecture = lecture;
    }

    @Override
    public Lecture getOwnedLectureOrThrow(String lectureId) {
      return this.lecture;
    }
  }

  private static class InMemoryLectureEnrollmentRepository implements LectureEnrollmentRepository {

    private final List<String> studentIds;

    private InMemoryLectureEnrollmentRepository(List<String> studentIds) {
      this.studentIds = List.copyOf(studentIds);
    }

    @Override
    public void save(LectureEnrollment enrollment) {
      throw new UnsupportedOperationException("Not needed for this test");
    }

    @Override
    public long countByLectureId(LectureId lectureId) {
      return this.studentIds.size();
    }

    @Override
    public List<String> findStudentIdsByLectureId(LectureId lectureId) {
      return this.studentIds;
    }

    @Override
    public List<LectureEnrollment> findByStudentId(String studentId) {
      return List.of();
    }

    @Override
    public boolean existsByLectureIdAndStudentId(LectureId lectureId, String studentId) {
      return this.studentIds.contains(studentId);
    }

    @Override
    public Optional<LectureEnrollment> findByLectureIdAndStudentId(
      LectureId lectureId,
      String studentId
    ) {
      return Optional.empty();
    }
  }

  private static class InMemoryStudentIdentityRepository implements StudentIdentityRepository {

    private final Map<String, StudentIdentity> identitiesByStudentId = new HashMap<>();

    private InMemoryStudentIdentityRepository(List<StudentIdentity> identities) {
      identities.forEach(identity -> this.identitiesByStudentId.put(identity.studentId(), identity));
    }

    @Override
    public void save(StudentIdentity identity) {
      this.identitiesByStudentId.put(identity.studentId(), identity);
    }

    @Override
    public Optional<StudentIdentity> findByStudentId(String studentId) {
      return Optional.ofNullable(this.identitiesByStudentId.get(studentId));
    }

    @Override
    public List<StudentIdentity> findByStudentIds(Collection<String> studentIds) {
      return studentIds
        .stream()
        .map(this.identitiesByStudentId::get)
        .filter(identity -> identity != null)
        .toList();
    }

    @Override
    public Optional<StudentIdentity> findByEmail(String email) {
      return this.identitiesByStudentId.values().stream().filter(identity -> email.equals(identity.email())).findFirst();
    }
  }

  private static class InMemorySubmissionRepository implements SubmissionRepository {

    private final List<Submission> submissions;

    private InMemorySubmissionRepository(List<Submission> submissions) {
      this.submissions = new ArrayList<>(submissions);
    }

    @Override
    public void save(Submission submission) {
      this.submissions.add(submission);
    }

    @Override
    public Optional<Submission> findById(SubmissionId submissionId) {
      return this.submissions.stream().filter(submission -> submission.id().equals(submissionId)).findFirst();
    }

    @Override
    public Optional<Submission> findLatestByLectureQuestionAndStudent(
      LectureId lectureId,
      QuestionId questionId,
      String studentId
    ) {
      return this.submissions
        .stream()
        .filter(submission -> submission.lectureId().equals(lectureId))
        .filter(submission -> submission.questionId().equals(questionId))
        .filter(submission -> submission.studentId().equals(studentId))
        .max(Comparator.comparing(Submission::timestamp));
    }

    @Override
    public List<Submission> findByLectureAndQuestion(LectureId lectureId, QuestionId questionId) {
      return this.submissions
        .stream()
        .filter(submission -> submission.lectureId().equals(lectureId))
        .filter(submission -> submission.questionId().equals(questionId))
        .toList();
    }

    @Override
    public long countByLectureQuestionAndStudent(
      LectureId lectureId,
      QuestionId questionId,
      String studentId
    ) {
      return this.submissions
        .stream()
        .filter(submission -> submission.lectureId().equals(lectureId))
        .filter(submission -> submission.questionId().equals(questionId))
        .filter(submission -> submission.studentId().equals(studentId))
        .count();
    }

    @Override
    public Set<String> findSubmittedQuestionIdsByLectureAndStudent(
      LectureId lectureId,
      String studentId
    ) {
      return this.submissions
        .stream()
        .filter(submission -> submission.lectureId().equals(lectureId))
        .filter(submission -> submission.studentId().equals(studentId))
        .map(submission -> submission.questionId().value())
        .collect(Collectors.toSet());
    }

    @Override
    public List<QuestionStudentAttempt> findQuestionStudentAttemptsByLecture(LectureId lectureId) {
      return List.of();
    }
  }
}
