package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AddQuestionToLectureUseCase {

  private final LectureRepository lectureRepository;

  public AddQuestionToLectureUseCase(LectureRepository lectureRepository) {
    this.lectureRepository = lectureRepository;
  }

  public Lecture execute(
    String lectureId,
    String questionId,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds
  ) {
    Lecture lecture = this.lectureRepository
      .findById(new LectureId(lectureId))
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    String resolvedQuestionId =
      questionId == null || questionId.isBlank() ? UUID.randomUUID().toString() : questionId;
    Lecture updatedLecture = lecture.addQuestion(
      resolvedQuestionId,
      prompt,
      modelAnswer,
      timeLimitSeconds
    );
    this.lectureRepository.save(updatedLecture);
    return updatedLecture;
  }
}
