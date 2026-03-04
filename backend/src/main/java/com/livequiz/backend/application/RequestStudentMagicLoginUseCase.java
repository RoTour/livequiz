package com.livequiz.backend.application;

import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RequestStudentMagicLoginUseCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    RequestStudentMagicLoginUseCase.class
  );

  private static final String EMAIL_VERIFICATION_COOLDOWN =
    "EMAIL_VERIFICATION_COOLDOWN";
  private static final String EMAIL_VERIFICATION_RATE_LIMITED =
    "EMAIL_VERIFICATION_RATE_LIMITED";

  public record RequestLoginResult(String status) {}

  private static final RequestLoginResult GENERIC_SUCCESS = new RequestLoginResult(
    "VERIFICATION_EMAIL_SENT_IF_ALLOWED"
  );

  private final StudentIdentityRepository studentIdentityRepository;
  private final StudentEmailPolicy studentEmailPolicy;
  private final EmailVerificationChallengeService emailVerificationChallengeService;

  public RequestStudentMagicLoginUseCase(
    StudentIdentityRepository studentIdentityRepository,
    StudentEmailPolicy studentEmailPolicy,
    EmailVerificationChallengeService emailVerificationChallengeService
  ) {
    this.studentIdentityRepository = studentIdentityRepository;
    this.studentEmailPolicy = studentEmailPolicy;
    this.emailVerificationChallengeService = emailVerificationChallengeService;
  }

  public RequestLoginResult execute(String email) {
    String normalizedEmail = this.studentEmailPolicy.normalizeAndValidate(email);

    StudentIdentity identity = this.studentIdentityRepository.findByEmail(normalizedEmail).orElse(null);
    if (identity == null) {
      return GENERIC_SUCCESS;
    }

    try {
      this.emailVerificationChallengeService.issueFor(identity.studentId(), normalizedEmail);
    } catch (RuntimeException e) {
      if (e instanceof ApiException apiException) {
        if (isNonEnumeratingThrottleError(apiException.code())) {
          LOGGER.info(
            "Student magic-link request throttled studentId={} email={} code={}",
            identity.studentId(),
            normalizedEmail,
            apiException.code()
          );
          return GENERIC_SUCCESS;
        }
      }

      LOGGER.warn(
        "Student magic-link request failed but returned generic response studentId={} email={}",
        identity.studentId(),
        normalizedEmail,
        e
      );
      return GENERIC_SUCCESS;
    }
    return GENERIC_SUCCESS;
  }

  private boolean isNonEnumeratingThrottleError(String code) {
    return EMAIL_VERIFICATION_COOLDOWN.equals(code) ||
    EMAIL_VERIFICATION_RATE_LIMITED.equals(code);
  }
}
