package com.livequiz.backend.domain.submission;

import java.util.List;

public record Feedback(boolean isCorrect, List<String> missingKeyPoints, String comment) {
  public Feedback {
    if (missingKeyPoints == null) {
      missingKeyPoints = List.of();
    }
    if (comment == null) {
      comment = "";
    }
  }
}
