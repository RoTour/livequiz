package com.livequiz.backend.infrastructure.web;

import java.time.LocalDateTime;

public record ErrorResponse(String code, String message, String timestamp) {
  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message, LocalDateTime.now().toString());
  }
}
