package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureRepository;
import org.springframework.stereotype.Service;

@Service
public class UnlockQuestionUseCase {

  private final LectureRepository lectureRepository;
  private final InstructorLectureAccessService instructorLectureAccessService;

  public UnlockQuestionUseCase(
    LectureRepository lectureRepository,
    InstructorLectureAccessService instructorLectureAccessService
  ) {
    this.lectureRepository = lectureRepository;
    this.instructorLectureAccessService = instructorLectureAccessService;
  }

  public Lecture execute(String lectureId, String questionId) {
    Lecture lecture = this.instructorLectureAccessService.getOwnedLectureOrThrow(lectureId);

    Lecture updatedLecture = lecture.unlockQuestion(questionId);
    this.lectureRepository.save(updatedLecture);
    return updatedLecture;
  }
}
