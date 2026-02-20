package com.livequiz.backend.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "livequiz")
public record LiveQuizProperties(
  int submissionCooldownSeconds,
  String inviteBaseUrl,
  int inviteExpirationHours
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
  }
}
