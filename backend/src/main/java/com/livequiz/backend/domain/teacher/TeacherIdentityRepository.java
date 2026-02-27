package com.livequiz.backend.domain.teacher;

import java.util.Optional;

public interface TeacherIdentityRepository {
  void save(TeacherIdentity identity);

  Optional<TeacherIdentity> findByPrincipalId(String principalId);
}
