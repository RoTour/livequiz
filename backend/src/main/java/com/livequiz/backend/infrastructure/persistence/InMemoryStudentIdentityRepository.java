package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryStudentIdentityRepository implements StudentIdentityRepository {

  private final Map<String, StudentIdentity> identitiesByStudentId = new ConcurrentHashMap<>();
  private final Map<String, String> studentIdByEmail = new ConcurrentHashMap<>();

  @Override
  public void save(StudentIdentity identity) {
    StudentIdentity existingIdentity = this.identitiesByStudentId.get(identity.studentId());
    if (existingIdentity != null && existingIdentity.email() != null) {
      this.studentIdByEmail.remove(existingIdentity.email());
    }

    this.identitiesByStudentId.put(identity.studentId(), identity);
    if (identity.email() != null) {
      this.studentIdByEmail.put(normalizeEmail(identity.email()), identity.studentId());
    }
  }

  @Override
  public Optional<StudentIdentity> findByStudentId(String studentId) {
    return Optional.ofNullable(this.identitiesByStudentId.get(studentId));
  }

  @Override
  public Optional<StudentIdentity> findByEmail(String email) {
    if (email == null) {
      return Optional.empty();
    }
    String studentId = this.studentIdByEmail.get(normalizeEmail(email));
    if (studentId == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.identitiesByStudentId.get(studentId));
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
