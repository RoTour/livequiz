package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.lecture.Lecture;
import com.livequiz.backend.domain.lecture.LectureRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("in-memory")
public class InMemoryLectureRepository implements LectureRepository {

  public final Map<String, Lecture> lectures = new ConcurrentHashMap<>();

  @Override
  public void save(Lecture lecture) {
    this.lectures.put(lecture.id().value(), lecture);
  }
}
