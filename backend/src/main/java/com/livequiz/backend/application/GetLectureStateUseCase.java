package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GetLectureStateUseCase {

  private final LectureRepository lectureRepository;

  public GetLectureStateUseCase(LectureRepository lectureRepository) {
    this.lectureRepository = lectureRepository;
  }

  public Lecture execute(String lectureId) {
    return this.lectureRepository
      .findById(new LectureId(lectureId))
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );
  }
}
