package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.student.EmailVerificationChallenge;
import com.livequiz.backend.domain.student.EmailVerificationChallengeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresEmailVerificationChallengeRepository
  implements EmailVerificationChallengeRepository {

  private final JpaEmailVerificationChallengeRepository jpaEmailVerificationChallengeRepository;

  public JpaPostgresEmailVerificationChallengeRepository(
    JpaEmailVerificationChallengeRepository jpaEmailVerificationChallengeRepository
  ) {
    this.jpaEmailVerificationChallengeRepository = jpaEmailVerificationChallengeRepository;
  }

  @Override
  public void save(EmailVerificationChallenge challenge) {
    this.jpaEmailVerificationChallengeRepository.save(toEntity(challenge));
  }

  @Override
  public Optional<EmailVerificationChallenge> findLatestByTokenHash(String tokenHash) {
    return this.jpaEmailVerificationChallengeRepository
      .findFirstByTokenHashOrderByCreatedAtDesc(tokenHash)
      .map(this::toDomain);
  }

  @Override
  public List<EmailVerificationChallenge> findByStudentId(String studentId) {
    return this.jpaEmailVerificationChallengeRepository
      .findByStudentIdOrderByCreatedAtDesc(studentId)
      .stream()
      .map(this::toDomain)
      .toList();
  }

  private EmailVerificationChallengeEntity toEntity(EmailVerificationChallenge challenge) {
    return new EmailVerificationChallengeEntity(
      challenge.challengeId(),
      challenge.studentId(),
      challenge.email(),
      challenge.tokenHash(),
      challenge.expiresAt(),
      challenge.consumedAt(),
      challenge.createdAt()
    );
  }

  private EmailVerificationChallenge toDomain(EmailVerificationChallengeEntity entity) {
    return new EmailVerificationChallenge(
      entity.getChallengeId(),
      entity.getStudentId(),
      entity.getEmail(),
      entity.getTokenHash(),
      entity.getExpiresAt(),
      entity.getConsumedAt(),
      entity.getCreatedAt()
    );
  }
}
