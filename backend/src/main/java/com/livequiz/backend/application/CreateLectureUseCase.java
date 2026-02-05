package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateLectureUseCase {

  private final LectureRepository lectureRepository;

  public CreateLectureUseCase(LectureRepository lectureRepository) {
    this.lectureRepository = lectureRepository;
  }

  public Lecture createLecture(String lectureId, String title) {
    if (lectureId == null || lectureId.isBlank()) {
      lectureId = UUID.randomUUID().toString();
    }
    Lecture lecture = new Lecture(new LectureId(lectureId), title);
    this.lectureRepository.save(lecture);
    return lecture;
  }
}
