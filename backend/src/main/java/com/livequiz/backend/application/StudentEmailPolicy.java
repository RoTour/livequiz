package com.livequiz.backend.application;

import com.livequiz.backend.infrastructure.web.ApiException;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StudentEmailPolicy {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[a-z0-9._%+-]+@[a-z0-9-]+(?:\\.[a-z0-9-]+)+$"
  );

  private final LiveQuizProperties liveQuizProperties;

  public StudentEmailPolicy(LiveQuizProperties liveQuizProperties) {
    this.liveQuizProperties = liveQuizProperties;
  }

  public String normalizeAndValidate(String email) {
    if (email == null || email.isBlank()) {
      throw new ApiException(
        HttpStatus.BAD_REQUEST,
        "EMAIL_REQUIRED",
        "Email is required"
      );
    }

    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
      throw new ApiException(
        HttpStatus.BAD_REQUEST,
        "EMAIL_INVALID_FORMAT",
        "Email format is invalid"
      );
    }

    int atIndex = normalizedEmail.lastIndexOf('@');
    String emailDomain = normalizedEmail.substring(atIndex + 1);
    String allowedDomain = this.liveQuizProperties
      .studentEmailAllowedDomain()
      .trim()
      .toLowerCase(Locale.ROOT);
    if (!emailDomain.equals(allowedDomain)) {
      throw new ApiException(
        HttpStatus.BAD_REQUEST,
        "EMAIL_DOMAIN_NOT_ALLOWED",
        "Only school email addresses are allowed"
      );
    }

    return normalizedEmail;
  }
}
