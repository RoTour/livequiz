package com.livequiz.backend.application;

import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.domain.student.StudentIdentityStatus;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ResendStudentVerificationUseCase {

  public record ResendResult(String status) {}

  private final StudentIdentityRepository studentIdentityRepository;
  private final StudentEmailPolicy studentEmailPolicy;
  private final EmailVerificationChallengeService emailVerificationChallengeService;

  public ResendStudentVerificationUseCase(
    StudentIdentityRepository studentIdentityRepository,
    StudentEmailPolicy studentEmailPolicy,
    EmailVerificationChallengeService emailVerificationChallengeService
  ) {
    this.studentIdentityRepository = studentIdentityRepository;
    this.studentEmailPolicy = studentEmailPolicy;
    this.emailVerificationChallengeService = emailVerificationChallengeService;
  }

  public ResendResult execute(String studentId, String email) {
    Instant now = Instant.now();
    StudentIdentity identity = this.studentIdentityRepository
      .findByStudentId(studentId)
      .orElse(StudentIdentity.anonymous(studentId, now));

    String normalizedEmail;
    if (email != null && !email.isBlank()) {
      normalizedEmail = this.studentEmailPolicy.normalizeAndValidate(email);
      if (isEmailOwnedByOtherStudent(normalizedEmail, studentId)) {
        return genericSuccess();
      }
      identity = identity.registerEmail(normalizedEmail, now);
      try {
        this.studentIdentityRepository.save(identity);
      } catch (DataIntegrityViolationException e) {
        return genericSuccess();
      }
    } else {
      if (identity.status() == StudentIdentityStatus.REGISTERED_VERIFIED) {
        return genericSuccess();
      }
      normalizedEmail = identity.email();
      if (normalizedEmail == null || normalizedEmail.isBlank()) {
        throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "EMAIL_REQUIRED",
          "Email is required"
        );
      }
    }

    this.emailVerificationChallengeService.issueFor(identity.studentId(), normalizedEmail);
    return genericSuccess();
  }

  private boolean isEmailOwnedByOtherStudent(String normalizedEmail, String studentId) {
    return this.studentIdentityRepository
      .findByEmail(normalizedEmail)
      .filter(existingIdentity -> !existingIdentity.studentId().equals(studentId))
      .isPresent();
  }

  private ResendResult genericSuccess() {
    return new ResendResult("VERIFICATION_EMAIL_SENT_IF_ALLOWED");
  }
}
