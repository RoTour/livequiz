package com.livequiz.backend.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudentIdentityRepository
  extends JpaRepository<StudentIdentityEntity, String> {
  List<StudentIdentityEntity> findByStudentIdIn(Collection<String> studentIds);

  Optional<StudentIdentityEntity> findByEmail(String email);
}
