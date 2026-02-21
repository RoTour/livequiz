package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AddQuestionToLectureUseCase {

  private final LectureRepository lectureRepository;
  private final InstructorLectureAccessService instructorLectureAccessService;

  public AddQuestionToLectureUseCase(
    LectureRepository lectureRepository,
    InstructorLectureAccessService instructorLectureAccessService
  ) {
    this.lectureRepository = lectureRepository;
    this.instructorLectureAccessService = instructorLectureAccessService;
  }

  public Lecture execute(
    String lectureId,
    String questionId,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds
  ) {
    Lecture lecture = this.instructorLectureAccessService.getOwnedLectureOrThrow(lectureId);

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
