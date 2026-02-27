package com.livequiz.backend.domain.instructor;

import java.util.Optional;

public interface InstructorAccountRepository {
  void save(InstructorAccount account);

  Optional<InstructorAccount> findByEmail(String email);
}
