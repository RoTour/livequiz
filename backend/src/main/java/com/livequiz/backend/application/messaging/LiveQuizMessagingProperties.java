package com.livequiz.backend.application.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "livequiz.messaging")
public record LiveQuizMessagingProperties(
  boolean enabled,
  String exchange,
  Email email,
  Submission submission,
  Outbox outbox
) {
  public LiveQuizMessagingProperties {
    if (exchange == null || exchange.isBlank()) {
      exchange = "livequiz.events";
    }
    if (email == null) {
      email = new Email(
        "livequiz.email.dispatch.v1",
        "livequiz.email.dispatch.retry.v1",
        "livequiz.email.dispatch.dlq.v1",
        "email.dispatch.requested.v1",
        "email.dispatch.retry.v1",
        "email.dispatch.failed.v1",
        6,
        1
      );
    }
    if (submission == null) {
      submission = new Submission(
        "livequiz.submission.evaluate.v1",
        "livequiz.submission.evaluate.retry.v1",
        "livequiz.submission.evaluate.dlq.v1",
        "submission.evaluate.requested.v1",
        "submission.evaluate.retry.v1",
        "submission.evaluate.failed.v1",
        6,
        2
      );
    }
    if (outbox == null) {
      outbox = new Outbox(500, 100);
    }
  }

  public record Email(
    String mainQueue,
    String retryQueue,
    String dlq,
    String requestedRoutingKey,
    String retryRoutingKey,
    String failedRoutingKey,
    int maxAttempts,
    int consumerConcurrency
  ) {
    public Email {
      if (mainQueue == null || mainQueue.isBlank()) {
        mainQueue = "livequiz.email.dispatch.v1";
      }
      if (retryQueue == null || retryQueue.isBlank()) {
        retryQueue = "livequiz.email.dispatch.retry.v1";
      }
      if (dlq == null || dlq.isBlank()) {
        dlq = "livequiz.email.dispatch.dlq.v1";
      }
      if (requestedRoutingKey == null || requestedRoutingKey.isBlank()) {
        requestedRoutingKey = "email.dispatch.requested.v1";
      }
      if (retryRoutingKey == null || retryRoutingKey.isBlank()) {
        retryRoutingKey = "email.dispatch.retry.v1";
      }
      if (failedRoutingKey == null || failedRoutingKey.isBlank()) {
        failedRoutingKey = "email.dispatch.failed.v1";
      }
      if (maxAttempts <= 0) {
        maxAttempts = 6;
      }
      if (consumerConcurrency <= 0) {
        consumerConcurrency = 1;
      }
    }
  }

  public record Submission(
    String mainQueue,
    String retryQueue,
    String dlq,
    String requestedRoutingKey,
    String retryRoutingKey,
    String failedRoutingKey,
    int maxAttempts,
    int consumerConcurrency
  ) {
    public Submission {
      if (mainQueue == null || mainQueue.isBlank()) {
        mainQueue = "livequiz.submission.evaluate.v1";
      }
      if (retryQueue == null || retryQueue.isBlank()) {
        retryQueue = "livequiz.submission.evaluate.retry.v1";
      }
      if (dlq == null || dlq.isBlank()) {
        dlq = "livequiz.submission.evaluate.dlq.v1";
      }
      if (requestedRoutingKey == null || requestedRoutingKey.isBlank()) {
        requestedRoutingKey = "submission.evaluate.requested.v1";
      }
      if (retryRoutingKey == null || retryRoutingKey.isBlank()) {
        retryRoutingKey = "submission.evaluate.retry.v1";
      }
      if (failedRoutingKey == null || failedRoutingKey.isBlank()) {
        failedRoutingKey = "submission.evaluate.failed.v1";
      }
      if (maxAttempts <= 0) {
        maxAttempts = 6;
      }
      if (consumerConcurrency <= 0) {
        consumerConcurrency = 2;
      }
    }
  }

  public record Outbox(int pollIntervalMs, int batchSize) {
    public Outbox {
      if (pollIntervalMs <= 0) {
        pollIntervalMs = 500;
      }
      if (batchSize <= 0) {
        batchSize = 100;
      }
    }
  }
}
