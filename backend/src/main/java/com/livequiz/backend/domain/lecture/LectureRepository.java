package com.livequiz.backend.domain.lecture;

public interface LectureRepository {
  void save(Lecture lecture);

  java.util.Optional<Lecture> findById(LectureId lectureId);
}
