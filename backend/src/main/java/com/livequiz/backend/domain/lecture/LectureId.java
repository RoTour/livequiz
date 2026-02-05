package com.livequiz.backend.domain.lecture;

public record LectureId(String value) {
  public LectureId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Lecture ID cannot be empty");
    }
  }
}
// Old version without records:
//
// public class LectureId {
//     private final String value;
//     public LectureId(String value) {
//         this.value = value;
//     }
//     // No "get...", just the name of the field
//     public String value() { return value; }
//     // Automatic equals() and hashCode() based on 'value'
//     public boolean equals(Object o) { ... }
//     public int hashCode() { ... }
//     // Automatic toString() -> "LectureId[value=...]"
//     public String toString() { ... }
// }
