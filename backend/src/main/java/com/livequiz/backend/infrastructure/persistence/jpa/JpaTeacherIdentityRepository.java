package com.livequiz.backend.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTeacherIdentityRepository
  extends JpaRepository<TeacherIdentityEntity, String> {}
