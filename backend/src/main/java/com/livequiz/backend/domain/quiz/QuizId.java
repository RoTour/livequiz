package com.livequiz.backend.domain.quiz;

public record QuizId(String value) {
  public QuizId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Quiz ID cannot be empty");
    }
  }
}
// Old version without records:
//
// public class QuizId {
//     private final String value;
//     public QuizId(String value) {
//         this.value = value;
//     }
//     // No "get...", just the name of the field
//     public String value() { return value; }
//     // Automatic equals() and hashCode() based on 'value'
//     public boolean equals(Object o) { ... }
//     public int hashCode() { ... }
//     // Automatic toString() -> "QuizId[value=...]"
//     public String toString() { ... }
// }
