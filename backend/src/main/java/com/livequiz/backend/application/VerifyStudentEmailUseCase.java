package com.livequiz.backend.application;

import com.livequiz.backend.domain.student.EmailVerificationChallenge;
import com.livequiz.backend.domain.student.EmailVerificationChallengeRepository;
import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import com.livequiz.backend.infrastructure.web.jwt.JwtService;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class VerifyStudentEmailUseCase {

  public record VerifyResult(String token) {}

  private final EmailVerificationChallengeRepository challengeRepository;
  private final StudentIdentityRepository studentIdentityRepository;
  private final StudentVerificationTokenService tokenService;
  private final JwtService jwtService;

  public VerifyStudentEmailUseCase(
    EmailVerificationChallengeRepository challengeRepository,
    StudentIdentityRepository studentIdentityRepository,
    StudentVerificationTokenService tokenService,
    JwtService jwtService
  ) {
    this.challengeRepository = challengeRepository;
    this.studentIdentityRepository = studentIdentityRepository;
    this.tokenService = tokenService;
    this.jwtService = jwtService;
  }

  public VerifyResult execute(String token) {
    if (token == null || token.isBlank()) {
      throw new ApiException(
        HttpStatus.BAD_REQUEST,
        "EMAIL_VERIFICATION_TOKEN_REQUIRED",
        "Verification token is required"
      );
    }

    String tokenHash = this.tokenService.hashToken(token);
    EmailVerificationChallenge challenge = this.challengeRepository
      .findLatestByTokenHash(tokenHash)
      .orElseThrow(this::invalidToken);

    Instant now = Instant.now();
    if (challenge.isConsumed()) {
      throw new ApiException(
        HttpStatus.CONFLICT,
        "EMAIL_VERIFICATION_TOKEN_CONSUMED",
        "Verification token has already been used"
      );
    }
    if (challenge.isExpiredAt(now)) {
      throw new ApiException(
        HttpStatus.GONE,
        "EMAIL_VERIFICATION_TOKEN_EXPIRED",
        "Verification token has expired"
      );
    }

    StudentIdentity identity = this.studentIdentityRepository
      .findByStudentId(challenge.studentId())
      .orElseThrow(this::invalidToken);

    if (identity.email() == null || !identity.email().equals(challenge.email())) {
      throw invalidToken();
    }

    EmailVerificationChallenge consumedChallenge = challenge.consume(now);
    this.challengeRepository.save(consumedChallenge);
    StudentIdentity verifiedIdentity = identity.verifyEmail(now);
    this.studentIdentityRepository.save(verifiedIdentity);

    String verifiedStudentToken = this.jwtService.createStudentToken(
        verifiedIdentity.studentId(),
        false,
        true
      );
    return new VerifyResult(verifiedStudentToken);
  }

  private ApiException invalidToken() {
    return new ApiException(
      HttpStatus.BAD_REQUEST,
      "EMAIL_VERIFICATION_TOKEN_INVALID",
      "Verification token is invalid"
    );
  }
}
