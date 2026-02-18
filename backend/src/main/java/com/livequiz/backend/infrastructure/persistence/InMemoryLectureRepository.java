package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryLectureRepository implements LectureRepository {

  public final Map<String, Lecture> lectures = new ConcurrentHashMap<>();

  @Override
  public void save(Lecture lecture) {
    this.lectures.put(lecture.id().value(), lecture);
  }

  @Override
  public java.util.Optional<Lecture> findById(LectureId lectureId) {
    return java.util.Optional.ofNullable(this.lectures.get(lectureId.value()));
  }
}
