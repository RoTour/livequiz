package com.livequiz.backend.domain.student;

import java.util.Optional;

public interface StudentIdentityRepository {
  void save(StudentIdentity identity);

  Optional<StudentIdentity> findByStudentId(String studentId);

  Optional<StudentIdentity> findByEmail(String email);
}
