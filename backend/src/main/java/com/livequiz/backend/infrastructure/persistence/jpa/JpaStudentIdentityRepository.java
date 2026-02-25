package com.livequiz.backend.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudentIdentityRepository
  extends JpaRepository<StudentIdentityEntity, String> {
  Optional<StudentIdentityEntity> findByEmail(String email);
}
