package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ListInstructorLecturesUseCase {

  private final LectureRepository lectureRepository;
  private final CurrentUserService currentUserService;

  public ListInstructorLecturesUseCase(
    LectureRepository lectureRepository,
    CurrentUserService currentUserService
  ) {
    this.lectureRepository = lectureRepository;
    this.currentUserService = currentUserService;
  }

  public java.util.List<LectureSummary> execute() {
    String instructorId = this.currentUserService.requireUserId();
    return this.lectureRepository
      .findByCreatedByInstructorId(instructorId)
      .stream()
      .map(lecture ->
        new LectureSummary(
          lecture.id().value(),
          lecture.title(),
          lecture.createdAt(),
          lecture.questions().size(),
          lecture.unlockedQuestionIds().size()
        )
      )
      .toList();
  }

  public record LectureSummary(
    String lectureId,
    String title,
    Instant createdAt,
    int questionCount,
    int unlockedCount
  ) {}
}
