package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.application.CreateLectureUseCase;
import com.livequiz.backend.domain.lecture.Lecture;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures")
@CrossOrigin(origins = "*")
public class LectureController {

  private final CreateLectureUseCase createLectureUseCase;

  public LectureController(CreateLectureUseCase createLectureUseCase) {
    this.createLectureUseCase = createLectureUseCase;
  }

  public record CreateLectureRequestDTO(String lectureId, String title) {}

  @PostMapping
  @LogExecutionTime
  public Map<String, String> createLecture(
    @RequestBody CreateLectureRequestDTO createLectureRequestDTO
  ) {
    Lecture lecture = this.createLectureUseCase.createLecture(
      createLectureRequestDTO.lectureId(),
      createLectureRequestDTO.title()
    );
    return Map.of("lectureId", lecture.id().value());
  }
}
