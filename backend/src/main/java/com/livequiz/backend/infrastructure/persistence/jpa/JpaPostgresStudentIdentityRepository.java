package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.domain.student.StudentIdentityStatus;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresStudentIdentityRepository
  implements StudentIdentityRepository {

  private final JpaStudentIdentityRepository jpaStudentIdentityRepository;

  public JpaPostgresStudentIdentityRepository(
    JpaStudentIdentityRepository jpaStudentIdentityRepository
  ) {
    this.jpaStudentIdentityRepository = jpaStudentIdentityRepository;
  }

  @Override
  public void save(StudentIdentity identity) {
    this.jpaStudentIdentityRepository.save(toEntity(identity));
  }

  @Override
  public Optional<StudentIdentity> findByStudentId(String studentId) {
    return this.jpaStudentIdentityRepository.findById(studentId).map(this::toDomain);
  }

  @Override
  public Optional<StudentIdentity> findByEmail(String email) {
    return this.jpaStudentIdentityRepository.findByEmail(email).map(this::toDomain);
  }

  private StudentIdentityEntity toEntity(StudentIdentity identity) {
    return new StudentIdentityEntity(
      identity.studentId(),
      identity.email(),
      identity.status().name(),
      identity.emailVerifiedAt(),
      identity.createdAt(),
      identity.updatedAt()
    );
  }

  private StudentIdentity toDomain(StudentIdentityEntity entity) {
    return new StudentIdentity(
      entity.getStudentId(),
      entity.getEmail(),
      StudentIdentityStatus.valueOf(entity.getStatus()),
      entity.getEmailVerifiedAt(),
      entity.getCreatedAt(),
      entity.getUpdatedAt()
    );
  }
}
