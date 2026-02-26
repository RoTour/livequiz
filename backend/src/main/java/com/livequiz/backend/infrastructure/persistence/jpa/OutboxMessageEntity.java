package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "outbox_messages")
public class OutboxMessageEntity {

  @Id
  @Column(length = 255, nullable = false)
  private String id;

  @Column(length = 255, nullable = false)
  private String eventType;

  @Column(length = 255, nullable = false)
  private String routingKey;

  @Column(columnDefinition = "text", nullable = false)
  private String payloadJson;

  @Column(length = 255)
  private String correlationId;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant publishedAt;

  @Column(nullable = false)
  private int attemptCount;

  @Column(columnDefinition = "text")
  private String lastError;

  public OutboxMessageEntity() {}

  public OutboxMessageEntity(
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
    this.id = id;
    this.eventType = eventType;
    this.routingKey = routingKey;
    this.payloadJson = payloadJson;
    this.correlationId = correlationId;
    this.createdAt = createdAt;
    this.publishedAt = publishedAt;
    this.attemptCount = attemptCount;
    this.lastError = lastError;
  }

  public String getId() {
    return id;
  }

  public String getEventType() {
    return eventType;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public String getPayloadJson() {
    return payloadJson;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public String getLastError() {
    return lastError;
  }
}
