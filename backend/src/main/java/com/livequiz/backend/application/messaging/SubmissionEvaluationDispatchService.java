package com.livequiz.backend.application.messaging;

import com.livequiz.backend.domain.submission.Submission;

public interface SubmissionEvaluationDispatchService {
  void dispatch(Submission submission, String correlationId);
}
