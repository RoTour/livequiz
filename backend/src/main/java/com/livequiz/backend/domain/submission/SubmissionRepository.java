package com.livequiz.backend.domain.submission;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import java.util.Optional;
import java.util.Set;

public interface SubmissionRepository {
  record QuestionStudentAttempt(String questionId, String studentId, long attemptCount) {}

  void save(Submission submission);

  Optional<Submission> findLatestByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  );

  java.util.List<Submission> findByLectureAndQuestion(
    LectureId lectureId,
    QuestionId questionId
  );

  long countByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  );

  Set<String> findSubmittedQuestionIdsByLectureAndStudent(
    LectureId lectureId,
    String studentId
  );

  java.util.List<QuestionStudentAttempt> findQuestionStudentAttemptsByLecture(
    LectureId lectureId
  );
}
