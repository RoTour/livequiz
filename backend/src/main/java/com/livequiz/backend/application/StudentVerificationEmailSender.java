package com.livequiz.backend.application;

import java.time.Instant;

public interface StudentVerificationEmailSender {
  void sendVerificationEmail(String email, String token, String verificationUrl, Instant expiresAt);
}
