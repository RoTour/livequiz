package com.livequiz.backend.infrastructure.messaging;

import com.livequiz.backend.application.AnswerEvaluationProvider;
import com.livequiz.backend.application.AnswerEvaluationStatus;
import com.livequiz.backend.domain.submission.Feedback;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
  name = "livequiz.answer-evaluation.provider",
  havingValue = "deterministic",
  matchIfMissing = true
)
public class DeterministicAnswerEvaluationProvider
  implements AnswerEvaluationProvider {

  @Override
  public EvaluationResult evaluate(String prompt, String modelAnswer, String answerText) {
    String normalizedModel = normalize(modelAnswer);
    String normalizedAnswer = normalize(answerText);

    if (normalizedAnswer.isBlank() || normalizedAnswer.length() < 10) {
      return new EvaluationResult(
        AnswerEvaluationStatus.INCOMPLETE,
        new Feedback(false, List.of("answer-too-short"), "Answer is too short"),
        "deterministic"
      );
    }
    if (!normalizedModel.isBlank() && normalizedAnswer.contains(normalizedModel)) {
      return new EvaluationResult(
        AnswerEvaluationStatus.CORRECT,
        new Feedback(true, List.of(), "Answer matches expected model answer"),
        "deterministic"
      );
    }
    return new EvaluationResult(
      AnswerEvaluationStatus.NEEDS_IMPROVEMENT,
      new Feedback(false, List.of("model-answer-mismatch"), "Answer does not match expected answer"),
      "deterministic"
    );
  }

  private String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.trim().toLowerCase(Locale.ROOT);
  }
}
