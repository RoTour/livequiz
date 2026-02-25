package com.livequiz.backend.infrastructure.email;

import com.livequiz.backend.application.StudentVerificationEmailSender;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
  name = "livequiz.student-email-verification-smtp-enabled",
  havingValue = "false",
  matchIfMissing = true
)
public class LoggingStudentVerificationEmailSender
  implements StudentVerificationEmailSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    LoggingStudentVerificationEmailSender.class
  );

  @Override
  public void sendVerificationEmail(
    String email,
    String token,
    String verificationUrl,
    Instant expiresAt
  ) {
    LOGGER.info(
      "Dispatching student verification email email={} expiresAt={}",
      email,
      expiresAt
    );
  }
}
