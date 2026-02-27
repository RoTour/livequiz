package com.livequiz.backend.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.livequiz.backend.domain.teacher.TeacherIdentity;
import com.livequiz.backend.domain.teacher.TeacherIdentityRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ResolveUserRoleUseCaseTest {

  @Test
  void should_return_instructor_when_teacher_identity_exists() {
    InMemoryTeacherRepository repository = new InMemoryTeacherRepository();
    repository.save(TeacherIdentity.active("teacher@ynov.com", Instant.now()));
    ResolveUserRoleUseCase useCase = new ResolveUserRoleUseCase(repository);

    String role = useCase.execute("Teacher@ynov.com");

    assertEquals(ResolveUserRoleUseCase.INSTRUCTOR_ROLE, role);
  }

  @Test
  void should_return_student_when_teacher_identity_is_missing() {
    ResolveUserRoleUseCase useCase = new ResolveUserRoleUseCase(new InMemoryTeacherRepository());

    String role = useCase.execute("student@ynov.com");

    assertEquals(ResolveUserRoleUseCase.STUDENT_ROLE, role);
  }

  @Test
  void should_return_student_when_teacher_identity_is_inactive() {
    InMemoryTeacherRepository repository = new InMemoryTeacherRepository();
    repository.save(new TeacherIdentity("instructor", false, Instant.now(), Instant.now()));
    ResolveUserRoleUseCase useCase = new ResolveUserRoleUseCase(repository);

    String role = useCase.execute("instructor");

    assertEquals(ResolveUserRoleUseCase.STUDENT_ROLE, role);
  }

  @Test
  void should_return_student_for_blank_principal() {
    ResolveUserRoleUseCase useCase = new ResolveUserRoleUseCase(new InMemoryTeacherRepository());

    String role = useCase.execute("  ");

    assertEquals(ResolveUserRoleUseCase.STUDENT_ROLE, role);
  }

  private static class InMemoryTeacherRepository implements TeacherIdentityRepository {

    private final Map<String, TeacherIdentity> identitiesByPrincipalId = new HashMap<>();

    @Override
    public void save(TeacherIdentity identity) {
      this.identitiesByPrincipalId.put(normalize(identity.principalId()), identity);
    }

    @Override
    public Optional<TeacherIdentity> findByPrincipalId(String principalId) {
      if (principalId == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(this.identitiesByPrincipalId.get(normalize(principalId)));
    }

    private String normalize(String principalId) {
      return principalId.trim().toLowerCase(Locale.ROOT);
    }
  }
}
