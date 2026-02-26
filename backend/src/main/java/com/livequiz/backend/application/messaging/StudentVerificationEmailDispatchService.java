package com.livequiz.backend.application.messaging;

import java.time.Instant;

public interface StudentVerificationEmailDispatchService {
  void dispatch(
    String email,
    String verificationToken,
    String verificationUrl,
    Instant expiresAt,
    String correlationId
  );
}
