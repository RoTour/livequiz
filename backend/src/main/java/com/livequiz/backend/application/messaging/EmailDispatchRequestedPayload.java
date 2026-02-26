package com.livequiz.backend.application.messaging;

public record EmailDispatchRequestedPayload(
  String messageId,
  String toEmail,
  String verificationToken,
  String verificationUrl,
  String expiresAt
) {}
