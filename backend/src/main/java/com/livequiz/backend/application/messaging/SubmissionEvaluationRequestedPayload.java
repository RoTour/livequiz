package com.livequiz.backend.application.messaging;

public record SubmissionEvaluationRequestedPayload(
  String messageId,
  String submissionId,
  String lectureId,
  String questionId,
  String studentId
) {}
