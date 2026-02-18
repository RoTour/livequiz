package com.livequiz.backend.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleDomainError(
    IllegalArgumentException e
  ) {
    ErrorResponse errorResponse = ErrorResponse.of(
      "DOMAIN_ERROR",
      e.getMessage()
    );
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
    ErrorResponse errorResponse = ErrorResponse.of(e.code(), e.getMessage());
    return ResponseEntity.status(e.status()).body(errorResponse);
  }

  @ExceptionHandler(SubmissionCooldownException.class)
  public ResponseEntity<ErrorResponse> handleSubmissionCooldownException(
    SubmissionCooldownException e
  ) {
    ErrorResponse errorResponse = ErrorResponse.of(
      e.code(),
      e.getMessage(),
      java.util.Map.of("retryAfterSeconds", e.retryAfterSeconds())
    );
    return ResponseEntity.status(e.status()).body(errorResponse);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundError(
    NoHandlerFoundException e
  ) {
    ErrorResponse errorResponse = ErrorResponse.of(
      "NOT_FOUND",
      "The requested resource was not found"
    );
    return ResponseEntity.status(404).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericError(Exception e) {
    ErrorResponse errorResponse = ErrorResponse.of(
      "INTERNAL_SERVER_ERROR",
      "An unexpected error occurred"
    );
    return ResponseEntity.status(500).body(errorResponse);
  }
}
