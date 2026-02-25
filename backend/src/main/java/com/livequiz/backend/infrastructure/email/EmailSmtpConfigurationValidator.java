package com.livequiz.backend.infrastructure.email;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EmailSmtpConfigurationValidator {

  private static final String DEFAULT_FROM_ADDRESS = "no-reply@livequiz.local";

  public EmailSmtpConfigurationValidator(
    @Value("${livequiz.student-email-verification-smtp-enabled:false}") boolean smtpEnabled,
    @Value("${livequiz.student-email-verification-from:no-reply@livequiz.local}") String fromAddress,
    Environment environment
  ) {
    if (!smtpEnabled) {
      return;
    }

    List<String> missingOrInvalid = new ArrayList<>();
    requireNonBlank(environment, "spring.mail.host", missingOrInvalid);
    requirePositivePort(environment.getProperty("spring.mail.port"), missingOrInvalid);
    requireNonBlank(environment, "spring.mail.username", missingOrInvalid);
    requireNonBlank(environment, "spring.mail.password", missingOrInvalid);
    requireTrue(
      environment,
      "spring.mail.properties.mail.smtp.auth",
      missingOrInvalid
    );
    requireTrue(
      environment,
      "spring.mail.properties.mail.smtp.starttls.enable",
      missingOrInvalid
    );
    if (
      fromAddress == null ||
      fromAddress.isBlank() ||
      DEFAULT_FROM_ADDRESS.equals(fromAddress)
    ) {
      missingOrInvalid.add(
        "livequiz.student-email-verification-from must be set to a verified sender address"
      );
    }

    if (!missingOrInvalid.isEmpty()) {
      throw new IllegalStateException(
        "SMTP email verification is enabled but configuration is invalid: " +
        String.join("; ", missingOrInvalid)
      );
    }
  }

  private void requireNonBlank(
    Environment environment,
    String key,
    List<String> missingOrInvalid
  ) {
    String value = environment.getProperty(key);
    if (value == null || value.isBlank()) {
      missingOrInvalid.add(key + " is required");
    }
  }

  private void requirePositivePort(
    String value,
    List<String> missingOrInvalid
  ) {
    if (value == null || value.isBlank()) {
      missingOrInvalid.add("spring.mail.port is required");
      return;
    }
    try {
      int port = Integer.parseInt(value);
      if (port <= 0) {
        missingOrInvalid.add("spring.mail.port must be a positive integer");
      }
    } catch (NumberFormatException e) {
      missingOrInvalid.add("spring.mail.port must be a valid integer");
    }
  }

  private void requireTrue(
    Environment environment,
    String key,
    List<String> missingOrInvalid
  ) {
    String value = environment.getProperty(key);
    if (!"true".equalsIgnoreCase(value)) {
      missingOrInvalid.add(key + " must be true");
    }
  }
}
