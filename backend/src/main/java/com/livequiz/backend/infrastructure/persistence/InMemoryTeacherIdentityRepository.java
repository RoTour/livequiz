package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.teacher.TeacherIdentity;
import com.livequiz.backend.domain.teacher.TeacherIdentityRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryTeacherIdentityRepository implements TeacherIdentityRepository {

  private final Map<String, TeacherIdentity> identitiesByPrincipalId = new ConcurrentHashMap<>();

  @Override
  public void save(TeacherIdentity identity) {
    this.identitiesByPrincipalId.put(normalize(identity.principalId()), identity);
  }

  @Override
  public Optional<TeacherIdentity> findByPrincipalId(String principalId) {
    if (principalId == null || principalId.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.identitiesByPrincipalId.get(normalize(principalId)));
  }

  private String normalize(String principalId) {
    return principalId.trim().toLowerCase(Locale.ROOT);
  }
}
