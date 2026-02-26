package com.livequiz.backend.application.messaging;

public record MessageEnvelope<T>(
  String messageId,
  String eventType,
  String schemaVersion,
  String correlationId,
  String causationId,
  String occurredAt,
  T payload
) {}
