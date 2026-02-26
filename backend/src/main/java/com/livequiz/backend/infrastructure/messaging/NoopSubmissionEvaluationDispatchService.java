package com.livequiz.backend.infrastructure.messaging;

import com.livequiz.backend.application.messaging.SubmissionEvaluationDispatchService;
import com.livequiz.backend.domain.submission.Submission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
  name = "livequiz.messaging.enabled",
  havingValue = "false",
  matchIfMissing = true
)
public class NoopSubmissionEvaluationDispatchService
  implements SubmissionEvaluationDispatchService {

  @Override
  public void dispatch(Submission submission, String correlationId) {}
}
