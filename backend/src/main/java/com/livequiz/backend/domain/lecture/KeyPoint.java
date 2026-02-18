package com.livequiz.backend.domain.lecture;

public record KeyPoint(String concept, String explanation) {
  public KeyPoint {
    if (concept == null || concept.isBlank()) {
      throw new IllegalArgumentException("Concept cannot be null or blank");
    }
  }
}
