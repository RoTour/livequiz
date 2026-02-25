package com.livequiz.backend.infrastructure.email;

import com.livequiz.backend.application.StudentVerificationEmailSender;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
  name = "livequiz.student-email-verification-smtp-enabled",
  havingValue = "true"
)
public class SmtpStudentVerificationEmailSender
  implements StudentVerificationEmailSender {

  private final JavaMailSender javaMailSender;
  private final String fromAddress;

  public SmtpStudentVerificationEmailSender(
    JavaMailSender javaMailSender,
    @Value("${livequiz.student-email-verification-from:no-reply@livequiz.local}") String fromAddress
  ) {
    this.javaMailSender = javaMailSender;
    this.fromAddress = fromAddress;
  }

  @Override
  public void sendVerificationEmail(
    String email,
    String token,
    String verificationUrl,
    Instant expiresAt
  ) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(this.fromAddress);
    message.setTo(email);
    message.setSubject("Verify your LiveQuiz student email");
    message.setText(
      "Use this link to verify your student email:\n" +
      verificationUrl +
      "\n\nThis link expires at " +
      expiresAt +
      "."
    );
    this.javaMailSender.send(message);
  }
}
