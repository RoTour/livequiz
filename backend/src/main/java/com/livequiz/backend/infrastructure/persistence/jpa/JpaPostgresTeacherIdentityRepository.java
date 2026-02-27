package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.teacher.TeacherIdentity;
import com.livequiz.backend.domain.teacher.TeacherIdentityRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresTeacherIdentityRepository
  implements TeacherIdentityRepository {

  private final JpaTeacherIdentityRepository jpaTeacherIdentityRepository;

  public JpaPostgresTeacherIdentityRepository(
    JpaTeacherIdentityRepository jpaTeacherIdentityRepository
  ) {
    this.jpaTeacherIdentityRepository = jpaTeacherIdentityRepository;
  }

  @Override
  public void save(TeacherIdentity identity) {
    this.jpaTeacherIdentityRepository.save(toEntity(identity));
  }

  @Override
  public Optional<TeacherIdentity> findByPrincipalId(String principalId) {
    if (principalId == null || principalId.isBlank()) {
      return Optional.empty();
    }
    String normalizedPrincipalId = principalId.trim().toLowerCase(Locale.ROOT);
    return this.jpaTeacherIdentityRepository.findById(normalizedPrincipalId).map(this::toDomain);
  }

  private TeacherIdentityEntity toEntity(TeacherIdentity identity) {
    return new TeacherIdentityEntity(
      identity.principalId(),
      identity.active(),
      identity.createdAt(),
      identity.updatedAt()
    );
  }

  private TeacherIdentity toDomain(TeacherIdentityEntity entity) {
    return new TeacherIdentity(
      entity.getPrincipalId(),
      entity.isActive(),
      entity.getCreatedAt(),
      entity.getUpdatedAt()
    );
  }
}
