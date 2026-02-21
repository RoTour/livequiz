package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureRepository;
import org.springframework.stereotype.Service;

@Service
public class UnlockNextQuestionUseCase {

  private final LectureRepository lectureRepository;
  private final InstructorLectureAccessService instructorLectureAccessService;

  public UnlockNextQuestionUseCase(
    LectureRepository lectureRepository,
    InstructorLectureAccessService instructorLectureAccessService
  ) {
    this.lectureRepository = lectureRepository;
    this.instructorLectureAccessService = instructorLectureAccessService;
  }

  public Lecture execute(String lectureId) {
    Lecture lecture = this.instructorLectureAccessService.getOwnedLectureOrThrow(lectureId);

    Lecture updatedLecture = lecture.unlockNextQuestion();
    this.lectureRepository.save(updatedLecture);
    return updatedLecture;
  }
}
