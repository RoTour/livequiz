package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import org.springframework.stereotype.Service;

@Service
public class GetLectureStateUseCase {

  private final InstructorLectureAccessService instructorLectureAccessService;

  public GetLectureStateUseCase(
    InstructorLectureAccessService instructorLectureAccessService
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
  }

  public Lecture execute(String lectureId) {
    return this.instructorLectureAccessService.getOwnedLectureOrThrow(lectureId);
  }
}
