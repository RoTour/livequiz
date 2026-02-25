package com.livequiz.backend.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailVerificationChallengeRepository
  extends JpaRepository<EmailVerificationChallengeEntity, String> {
  Optional<EmailVerificationChallengeEntity> findFirstByTokenHashOrderByCreatedAtDesc(
    String tokenHash
  );

  List<EmailVerificationChallengeEntity> findByStudentIdOrderByCreatedAtDesc(String studentId);
}
