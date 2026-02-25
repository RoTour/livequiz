package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.student.EmailVerificationChallenge;
import com.livequiz.backend.domain.student.EmailVerificationChallengeRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryEmailVerificationChallengeRepository
  implements EmailVerificationChallengeRepository {

  private final Map<String, EmailVerificationChallenge> challengesById = new ConcurrentHashMap<>();

  @Override
  public void save(EmailVerificationChallenge challenge) {
    this.challengesById.put(challenge.challengeId(), challenge);
  }

  @Override
  public Optional<EmailVerificationChallenge> findLatestByTokenHash(String tokenHash) {
    return this.challengesById
      .values()
      .stream()
      .filter(challenge -> challenge.tokenHash().equals(tokenHash))
      .max(Comparator.comparing(EmailVerificationChallenge::createdAt));
  }

  @Override
  public List<EmailVerificationChallenge> findByStudentId(String studentId) {
    return this.challengesById
      .values()
      .stream()
      .filter(challenge -> challenge.studentId().equals(studentId))
      .sorted(Comparator.comparing(EmailVerificationChallenge::createdAt).reversed())
      .toList();
  }
}
