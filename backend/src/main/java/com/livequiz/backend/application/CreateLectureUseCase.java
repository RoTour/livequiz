package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateLectureUseCase {

  private final LectureRepository lectureRepository;
  private final CurrentUserService currentUserService;

  public CreateLectureUseCase(
    LectureRepository lectureRepository,
    CurrentUserService currentUserService
  ) {
    this.lectureRepository = lectureRepository;
    this.currentUserService = currentUserService;
  }

  public Lecture createLecture(String lectureId, String title) {
    if (lectureId == null || lectureId.isBlank()) {
      lectureId = UUID.randomUUID().toString();
    }
    String instructorId = this.currentUserService.requireUserId();
    Lecture lecture = new Lecture(
      new LectureId(lectureId),
      title,
      instructorId,
      Instant.now()
    );
    this.lectureRepository.save(lecture);
    return lecture;
  }
}
