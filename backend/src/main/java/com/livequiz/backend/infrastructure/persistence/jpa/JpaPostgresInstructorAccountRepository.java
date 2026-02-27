package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.instructor.InstructorAccount;
import com.livequiz.backend.domain.instructor.InstructorAccountRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresInstructorAccountRepository
  implements InstructorAccountRepository {

  private final JpaInstructorAccountRepository jpaInstructorAccountRepository;

  public JpaPostgresInstructorAccountRepository(
    JpaInstructorAccountRepository jpaInstructorAccountRepository
  ) {
    this.jpaInstructorAccountRepository = jpaInstructorAccountRepository;
  }

  @Override
  public void save(InstructorAccount account) {
    this.jpaInstructorAccountRepository.save(toEntity(account));
  }

  @Override
  public Optional<InstructorAccount> findByEmail(String email) {
    if (email == null || email.isBlank()) {
      return Optional.empty();
    }

    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    return this.jpaInstructorAccountRepository.findById(normalizedEmail).map(this::toDomain);
  }

  private InstructorAccountEntity toEntity(InstructorAccount account) {
    return new InstructorAccountEntity(
      account.email(),
      account.passwordHash(),
      account.active(),
      account.createdAt(),
      account.updatedAt()
    );
  }

  private InstructorAccount toDomain(InstructorAccountEntity entity) {
    return new InstructorAccount(
      entity.getEmail(),
      entity.getPasswordHash(),
      entity.isActive(),
      entity.getCreatedAt(),
      entity.getUpdatedAt()
    );
  }
}
