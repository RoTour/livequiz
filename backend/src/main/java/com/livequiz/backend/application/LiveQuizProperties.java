package com.livequiz.backend.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "livequiz")
public record LiveQuizProperties(
  int submissionCooldownSeconds,
  String inviteBaseUrl,
  int inviteExpirationHours,
  boolean teacherRoleClassificationEnabled,
  String studentEmailAllowedDomain,
  int studentEmailVerificationTtlMinutes,
  int studentEmailVerificationResendCooldownSeconds,
  int studentEmailVerificationMaxPerHour,
  String studentEmailVerificationUrlBase
) {
  public LiveQuizProperties {
    if (submissionCooldownSeconds <= 0) {
      submissionCooldownSeconds = 30;
    }
    if (inviteBaseUrl == null || inviteBaseUrl.isBlank()) {
      inviteBaseUrl = "http://localhost:4200/student/join";
    }
    if (inviteExpirationHours <= 0 || inviteExpirationHours > 24) {
      inviteExpirationHours = 24;
    }
    if (studentEmailAllowedDomain == null || studentEmailAllowedDomain.isBlank()) {
      studentEmailAllowedDomain = "ynov.com";
    }
    if (
      studentEmailVerificationTtlMinutes <= 0 ||
      studentEmailVerificationTtlMinutes > 24 * 60
    ) {
      studentEmailVerificationTtlMinutes = 30;
    }
    if (studentEmailVerificationResendCooldownSeconds <= 0) {
      studentEmailVerificationResendCooldownSeconds = 60;
    }
    if (studentEmailVerificationMaxPerHour <= 0) {
      studentEmailVerificationMaxPerHour = 5;
    }
    if (studentEmailVerificationUrlBase == null || studentEmailVerificationUrlBase.isBlank()) {
      studentEmailVerificationUrlBase = "http://localhost:4200/student/verify-email";
    }
  }
}
