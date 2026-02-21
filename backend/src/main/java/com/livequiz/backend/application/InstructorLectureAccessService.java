package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class InstructorLectureAccessService {

  private final LectureRepository lectureRepository;
  private final CurrentUserService currentUserService;

  public InstructorLectureAccessService(
    LectureRepository lectureRepository,
    CurrentUserService currentUserService
  ) {
    this.lectureRepository = lectureRepository;
    this.currentUserService = currentUserService;
  }

  public Lecture getOwnedLectureOrThrow(String lectureId) {
    return getOwnedLectureOrThrow(new LectureId(lectureId));
  }

  public Lecture getOwnedLectureOrThrow(LectureId lectureId) {
    Lecture lecture = this.lectureRepository
      .findById(lectureId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    String instructorId = this.currentUserService.requireUserId();
    String ownerId = lecture.createdByInstructorId();
    if (ownerId == null || !ownerId.equals(instructorId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found");
    }

    return lecture;
  }
}
