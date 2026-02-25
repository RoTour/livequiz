package com.livequiz.backend.application;

import com.livequiz.backend.domain.student.EmailVerificationChallenge;
import com.livequiz.backend.domain.student.EmailVerificationChallengeRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationChallengeService {

  public record IssuedChallenge(EmailVerificationChallenge challenge, String token) {}

  private static final Logger LOGGER = LoggerFactory.getLogger(
    EmailVerificationChallengeService.class
  );

  private final EmailVerificationChallengeRepository challengeRepository;
  private final StudentVerificationTokenService tokenService;
  private final StudentVerificationEmailSender emailSender;
  private final LiveQuizProperties liveQuizProperties;

  public EmailVerificationChallengeService(
    EmailVerificationChallengeRepository challengeRepository,
    StudentVerificationTokenService tokenService,
    StudentVerificationEmailSender emailSender,
    LiveQuizProperties liveQuizProperties
  ) {
    this.challengeRepository = challengeRepository;
    this.tokenService = tokenService;
    this.emailSender = emailSender;
    this.liveQuizProperties = liveQuizProperties;
  }

  public IssuedChallenge issueFor(String studentId, String email) {
    Instant now = Instant.now();
    ensureThrottleAllowsChallenge(studentId, now);

    String token = this.tokenService.generateOpaqueToken();
    String tokenHash = this.tokenService.hashToken(token);
    Instant expiresAt = now.plus(
      Duration.ofMinutes(this.liveQuizProperties.studentEmailVerificationTtlMinutes())
    );

    EmailVerificationChallenge challenge = new EmailVerificationChallenge(
      UUID.randomUUID().toString(),
      studentId,
      email,
      tokenHash,
      expiresAt,
      null,
      now
    );

    String verificationUrl = buildVerificationUrl(token);
    this.emailSender.sendVerificationEmail(email, token, verificationUrl, expiresAt);
    this.challengeRepository.save(challenge);
    LOGGER.info(
      "Issued email verification challenge for studentId={} expiresAt={}",
      studentId,
      expiresAt
    );

    return new IssuedChallenge(challenge, token);
  }

  private void ensureThrottleAllowsChallenge(String studentId, Instant now) {
    java.util.List<EmailVerificationChallenge> challenges = this.challengeRepository.findByStudentId(
        studentId
      );
    challenges
      .stream()
      .max(Comparator.comparing(EmailVerificationChallenge::createdAt))
      .ifPresent(latestChallenge -> {
        Instant nextAllowedAt = latestChallenge
          .createdAt()
          .plusSeconds(this.liveQuizProperties.studentEmailVerificationResendCooldownSeconds());
        if (now.isBefore(nextAllowedAt)) {
          throw new ApiException(
            HttpStatus.TOO_MANY_REQUESTS,
            "EMAIL_VERIFICATION_COOLDOWN",
            "Please wait before requesting another verification email"
          );
        }
      });

    Instant oneHourAgo = now.minus(Duration.ofHours(1));
    long attemptsInPastHour = challenges
      .stream()
      .filter(challenge -> !challenge.createdAt().isBefore(oneHourAgo))
      .count();
    if (attemptsInPastHour >= this.liveQuizProperties.studentEmailVerificationMaxPerHour()) {
      throw new ApiException(
        HttpStatus.TOO_MANY_REQUESTS,
        "EMAIL_VERIFICATION_RATE_LIMITED",
        "Too many verification attempts. Please try again later"
      );
    }
  }

  private String buildVerificationUrl(String token) {
    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
    String baseUrl = this.liveQuizProperties.studentEmailVerificationUrlBase();
    if (baseUrl.contains("?")) {
      return baseUrl + "&token=" + encodedToken;
    }
    return baseUrl + "?token=" + encodedToken;
  }
}
