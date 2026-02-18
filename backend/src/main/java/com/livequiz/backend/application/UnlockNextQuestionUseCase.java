package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UnlockNextQuestionUseCase {

  private final LectureRepository lectureRepository;

  public UnlockNextQuestionUseCase(LectureRepository lectureRepository) {
    this.lectureRepository = lectureRepository;
  }

  public Lecture execute(String lectureId) {
    Lecture lecture = this.lectureRepository
      .findById(new LectureId(lectureId))
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    Lecture updatedLecture = lecture.unlockNextQuestion();
    this.lectureRepository.save(updatedLecture);
    return updatedLecture;
  }
}
