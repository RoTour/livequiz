package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_dispatch_jobs")
public class EmailDispatchJobEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String messageId;

  @Column(length = 320, nullable = false)
  private String toEmail;

  @Column(columnDefinition = "text", nullable = false)
  private String verificationToken;

  @Column(columnDefinition = "text", nullable = false)
  private String verificationUrl;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(length = 64, nullable = false)
  private String status;

  @Column(nullable = false)
  private int attemptCount;

  private Instant nextAttemptAt;

  @Column(columnDefinition = "text")
  private String lastError;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public EmailDispatchJobEntity() {}

  public EmailDispatchJobEntity(
    String messageId,
    String toEmail,
    String verificationToken,
    String verificationUrl,
    Instant expiresAt,
    String status,
    int attemptCount,
    Instant nextAttemptAt,
    String lastError,
    Instant createdAt,
    Instant updatedAt
  ) {
    this.messageId = messageId;
    this.toEmail = toEmail;
    this.verificationToken = verificationToken;
    this.verificationUrl = verificationUrl;
    this.expiresAt = expiresAt;
    this.status = status;
    this.attemptCount = attemptCount;
    this.nextAttemptAt = nextAttemptAt;
    this.lastError = lastError;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getToEmail() {
    return toEmail;
  }

  public String getVerificationToken() {
    return verificationToken;
  }

  public String getVerificationUrl() {
    return verificationUrl;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public String getStatus() {
    return status;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public String getLastError() {
    return lastError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
