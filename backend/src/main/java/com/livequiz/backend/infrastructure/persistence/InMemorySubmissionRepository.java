package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import com.livequiz.backend.domain.submission.Submission;
import com.livequiz.backend.domain.submission.SubmissionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemorySubmissionRepository implements SubmissionRepository {

  private final List<Submission> submissions = new CopyOnWriteArrayList<>();

  @Override
  public void save(Submission submission) {
    this.submissions.add(submission);
  }

  @Override
  public Optional<Submission> findLatestByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  ) {
    return this.submissions
      .stream()
      .filter(submission -> submission.lectureId().value().equals(lectureId.value()))
      .filter(submission -> submission.questionId().value().equals(questionId.value()))
      .filter(submission -> submission.studentId().equals(studentId))
      .max(Comparator.comparing(Submission::timestamp));
  }

  @Override
  public Set<String> findSubmittedQuestionIdsByLectureAndStudent(
    LectureId lectureId,
    String studentId
  ) {
    return this.submissions
      .stream()
      .filter(submission -> submission.lectureId().value().equals(lectureId.value()))
      .filter(submission -> submission.studentId().equals(studentId))
      .map(submission -> submission.questionId().value())
      .collect(java.util.stream.Collectors.toSet());
  }

  @Override
  public List<QuestionStudentAttempt> findQuestionStudentAttemptsByLecture(
    LectureId lectureId
  ) {
    return this.submissions
      .stream()
      .filter(submission -> submission.lectureId().value().equals(lectureId.value()))
      .collect(
        Collectors.groupingBy(submission ->
          submission.questionId().value() + "::" + submission.studentId()
        )
      )
      .entrySet()
      .stream()
      .map(entry -> {
        Submission sample = entry.getValue().get(0);
        return new QuestionStudentAttempt(
          sample.questionId().value(),
          sample.studentId(),
          entry.getValue().size()
        );
      })
      .toList();
  }
}
