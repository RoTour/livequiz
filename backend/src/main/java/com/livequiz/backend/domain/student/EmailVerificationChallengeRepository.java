package com.livequiz.backend.domain.student;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationChallengeRepository {
  void save(EmailVerificationChallenge challenge);

  Optional<EmailVerificationChallenge> findLatestByTokenHash(String tokenHash);

  List<EmailVerificationChallenge> findByStudentId(String studentId);
}
