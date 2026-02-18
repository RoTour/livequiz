package com.livequiz.backend.infrastructure.web;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
  String code,
  String message,
  String timestamp,
  Map<String, Object> details
) {
  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message, LocalDateTime.now().toString(), Map.of());
  }

  public static ErrorResponse of(
    String code,
    String message,
    Map<String, Object> details
  ) {
    return new ErrorResponse(code, message, LocalDateTime.now().toString(), details);
  }
}
