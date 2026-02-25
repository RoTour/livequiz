package com.livequiz.backend.infrastructure.email;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class EmailSmtpConfigurationValidatorTest {

  @Test
  void shouldAllowMissingSmtpSettingsWhenSmtpIsDisabled() {
    MockEnvironment environment = new MockEnvironment();

    assertDoesNotThrow(() ->
      new EmailSmtpConfigurationValidator(false, "no-reply@livequiz.local", environment)
    );
  }

  @Test
  void shouldFailFastWhenSmtpIsEnabledButRequiredSettingsAreMissing() {
    MockEnvironment environment = new MockEnvironment();

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      new EmailSmtpConfigurationValidator(true, "", environment)
    );

    assertTrue(exception.getMessage().contains("spring.mail.host is required"));
    assertTrue(exception.getMessage().contains("spring.mail.port is required"));
    assertTrue(exception.getMessage().contains("spring.mail.username is required"));
    assertTrue(exception.getMessage().contains("spring.mail.password is required"));
  }

  @Test
  void shouldAllowSmtpWhenAllRequiredSettingsAreValid() {
    MockEnvironment environment = new MockEnvironment()
      .withProperty("spring.mail.host", "email-smtp.eu-west-1.amazonaws.com")
      .withProperty("spring.mail.port", "587")
      .withProperty("spring.mail.username", "ses-user")
      .withProperty("spring.mail.password", "ses-password")
      .withProperty("spring.mail.properties.mail.smtp.auth", "true")
      .withProperty("spring.mail.properties.mail.smtp.starttls.enable", "true");

    assertDoesNotThrow(() ->
      new EmailSmtpConfigurationValidator(
        true,
        "no-reply@verified-domain.com",
        environment
      )
    );
  }
}
