package com.livequiz.backend.application;

import com.livequiz.backend.domain.submission.Feedback;

public interface AnswerEvaluationProvider {
  EvaluationResult evaluate(String prompt, String modelAnswer, String answerText);

  record EvaluationResult(AnswerEvaluationStatus status, Feedback feedback, String model) {}
}
