package com.livequiz.backend.application;

import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class RegisterStudentEmailUseCase {

  public record RegisterEmailResult(String status) {}

  private static final Logger LOGGER = LoggerFactory.getLogger(
    RegisterStudentEmailUseCase.class
  );

  private final StudentIdentityRepository studentIdentityRepository;
  private final StudentEmailPolicy studentEmailPolicy;
  private final EmailVerificationChallengeService emailVerificationChallengeService;

  public RegisterStudentEmailUseCase(
    StudentIdentityRepository studentIdentityRepository,
    StudentEmailPolicy studentEmailPolicy,
    EmailVerificationChallengeService emailVerificationChallengeService
  ) {
    this.studentIdentityRepository = studentIdentityRepository;
    this.studentEmailPolicy = studentEmailPolicy;
    this.emailVerificationChallengeService = emailVerificationChallengeService;
  }

  public RegisterEmailResult execute(String studentId, String email) {
    String normalizedEmail = this.studentEmailPolicy.normalizeAndValidate(email);
    Instant now = Instant.now();

    if (isEmailOwnedByOtherStudent(normalizedEmail, studentId)) {
      LOGGER.info("Email registration skipped for existing email ownership studentId={}", studentId);
      return genericSuccess();
    }

    StudentIdentity identity = this.studentIdentityRepository
      .findByStudentId(studentId)
      .orElse(StudentIdentity.anonymous(studentId, now));
    StudentIdentity updatedIdentity = identity.registerEmail(normalizedEmail, now);
    try {
      this.studentIdentityRepository.save(updatedIdentity);
    } catch (DataIntegrityViolationException e) {
      LOGGER.info(
        "Email registration raced with another owner studentId={} email={}",
        studentId,
        normalizedEmail
      );
      return genericSuccess();
    }

    this.emailVerificationChallengeService.issueFor(updatedIdentity.studentId(), normalizedEmail);
    return genericSuccess();
  }

  private boolean isEmailOwnedByOtherStudent(String normalizedEmail, String studentId) {
    return this.studentIdentityRepository
      .findByEmail(normalizedEmail)
      .filter(identity -> !identity.studentId().equals(studentId))
      .isPresent();
  }

  private RegisterEmailResult genericSuccess() {
    return new RegisterEmailResult("VERIFICATION_EMAIL_SENT_IF_ALLOWED");
  }
}
