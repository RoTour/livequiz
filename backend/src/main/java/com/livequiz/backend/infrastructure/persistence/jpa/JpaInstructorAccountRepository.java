package com.livequiz.backend.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInstructorAccountRepository
  extends JpaRepository<InstructorAccountEntity, String> {}
