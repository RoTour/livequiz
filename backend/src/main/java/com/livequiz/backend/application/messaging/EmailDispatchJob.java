package com.livequiz.backend.application.messaging;

import java.time.Instant;

public record EmailDispatchJob(
  String messageId,
  String toEmail,
  String verificationToken,
  String verificationUrl,
  Instant expiresAt,
  EmailDispatchStatus status,
  int attemptCount,
  Instant nextAttemptAt,
  String lastError,
  Instant createdAt,
  Instant updatedAt
) {
  public EmailDispatchJob {
    if (messageId == null || messageId.isBlank()) {
      throw new IllegalArgumentException("Email dispatch message ID cannot be blank");
    }
    if (toEmail == null || toEmail.isBlank()) {
      throw new IllegalArgumentException("Email dispatch target cannot be blank");
    }
    if (verificationToken == null || verificationToken.isBlank()) {
      throw new IllegalArgumentException("Verification token cannot be blank");
    }
    if (verificationUrl == null || verificationUrl.isBlank()) {
      throw new IllegalArgumentException("Verification URL cannot be blank");
    }
    if (expiresAt == null) {
      throw new IllegalArgumentException("Email expiration cannot be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("Email dispatch status cannot be null");
    }
    if (attemptCount < 0) {
      throw new IllegalArgumentException("Email dispatch attempt count cannot be negative");
    }
    if (createdAt == null || updatedAt == null) {
      throw new IllegalArgumentException("Email dispatch timestamps cannot be null");
    }
  }

  public EmailDispatchJob markSent(Instant now) {
    return new EmailDispatchJob(
      this.messageId,
      this.toEmail,
      this.verificationToken,
      this.verificationUrl,
      this.expiresAt,
      EmailDispatchStatus.SENT,
      this.attemptCount + 1,
      null,
      null,
      this.createdAt,
      now
    );
  }

  public EmailDispatchJob markRetryScheduled(Instant now, Instant nextAttemptAt, String error) {
    return new EmailDispatchJob(
      this.messageId,
      this.toEmail,
      this.verificationToken,
      this.verificationUrl,
      this.expiresAt,
      EmailDispatchStatus.RETRY_SCHEDULED,
      this.attemptCount + 1,
      nextAttemptAt,
      error,
      this.createdAt,
      now
    );
  }

  public EmailDispatchJob defer(Instant now, Instant nextAttemptAt, String error) {
    return new EmailDispatchJob(
      this.messageId,
      this.toEmail,
      this.verificationToken,
      this.verificationUrl,
      this.expiresAt,
      EmailDispatchStatus.RETRY_SCHEDULED,
      this.attemptCount,
      nextAttemptAt,
      error,
      this.createdAt,
      now
    );
  }

  public EmailDispatchJob markFailedFinal(Instant now, String error) {
    return new EmailDispatchJob(
      this.messageId,
      this.toEmail,
      this.verificationToken,
      this.verificationUrl,
      this.expiresAt,
      EmailDispatchStatus.FAILED_FINAL,
      this.attemptCount + 1,
      null,
      error,
      this.createdAt,
      now
    );
  }

  public EmailDispatchJob markProcessing(Instant now) {
    return new EmailDispatchJob(
      this.messageId,
      this.toEmail,
      this.verificationToken,
      this.verificationUrl,
      this.expiresAt,
      EmailDispatchStatus.PROCESSING,
      this.attemptCount,
      null,
      null,
      this.createdAt,
      now
    );
  }
}
