package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.teacher.TeacherIdentity;
import com.livequiz.backend.domain.teacher.TeacherIdentityRepository;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "in-memory", "memory" })
public class InMemoryTeacherIdentitySeeder {

  private final TeacherIdentityRepository teacherIdentityRepository;

  public InMemoryTeacherIdentitySeeder(TeacherIdentityRepository teacherIdentityRepository) {
    this.teacherIdentityRepository = teacherIdentityRepository;
  }

  @PostConstruct
  public void seedDefaultTeachers() {
    Instant now = Instant.now();
    this.teacherIdentityRepository.save(TeacherIdentity.active("instructor", now));
    this.teacherIdentityRepository.save(TeacherIdentity.active("instructor2", now));
  }
}
