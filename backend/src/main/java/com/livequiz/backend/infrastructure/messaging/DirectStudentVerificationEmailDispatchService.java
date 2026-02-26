package com.livequiz.backend.infrastructure.messaging;

import com.livequiz.backend.application.StudentVerificationEmailSender;
import com.livequiz.backend.application.messaging.StudentVerificationEmailDispatchService;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
  name = "livequiz.messaging.enabled",
  havingValue = "false",
  matchIfMissing = true
)
public class DirectStudentVerificationEmailDispatchService
  implements StudentVerificationEmailDispatchService {

  private final StudentVerificationEmailSender studentVerificationEmailSender;

  public DirectStudentVerificationEmailDispatchService(
    StudentVerificationEmailSender studentVerificationEmailSender
  ) {
    this.studentVerificationEmailSender = studentVerificationEmailSender;
  }

  @Override
  public void dispatch(
    String email,
    String verificationToken,
    String verificationUrl,
    Instant expiresAt,
    String correlationId
  ) {
    this.studentVerificationEmailSender.sendVerificationEmail(
        email,
        verificationToken,
        verificationUrl,
        expiresAt
      );
  }
}
