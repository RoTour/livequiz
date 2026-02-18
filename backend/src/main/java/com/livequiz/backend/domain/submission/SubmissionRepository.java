package com.livequiz.backend.domain.submission;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.QuestionId;
import java.util.Optional;
import java.util.Set;

public interface SubmissionRepository {
  void save(Submission submission);

  Optional<Submission> findLatestByLectureQuestionAndStudent(
    LectureId lectureId,
    QuestionId questionId,
    String studentId
  );

  Set<String> findSubmittedQuestionIdsByLectureAndStudent(
    LectureId lectureId,
    String studentId
  );
}
