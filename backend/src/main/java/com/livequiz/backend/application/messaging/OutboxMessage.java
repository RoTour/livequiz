package com.livequiz.backend.application.messaging;

import java.time.Instant;

public record OutboxMessage(
  String id,
  String eventType,
  String routingKey,
  String payloadJson,
  String correlationId,
  Instant createdAt,
  Instant publishedAt,
  int attemptCount,
  String lastError
) {
  public OutboxMessage {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Outbox message ID cannot be blank");
    }
    if (eventType == null || eventType.isBlank()) {
      throw new IllegalArgumentException("Outbox event type cannot be blank");
    }
    if (routingKey == null || routingKey.isBlank()) {
      throw new IllegalArgumentException("Outbox routing key cannot be blank");
    }
    if (payloadJson == null || payloadJson.isBlank()) {
      throw new IllegalArgumentException("Outbox payload cannot be blank");
    }
    if (createdAt == null) {
      throw new IllegalArgumentException("Outbox createdAt cannot be null");
    }
    if (attemptCount < 0) {
      throw new IllegalArgumentException("Outbox attempt count cannot be negative");
    }
  }

  public OutboxMessage markPublished(Instant now) {
    return new OutboxMessage(
      this.id,
      this.eventType,
      this.routingKey,
      this.payloadJson,
      this.correlationId,
      this.createdAt,
      now,
      this.attemptCount,
      this.lastError
    );
  }

  public OutboxMessage markPublishFailed(String error, Instant now) {
    return new OutboxMessage(
      this.id,
      this.eventType,
      this.routingKey,
      this.payloadJson,
      this.correlationId,
      this.createdAt,
      this.publishedAt,
      this.attemptCount + 1,
      error
    );
  }
}
