[CreateLectureUseCase](backend/src/main/java/com/livequiz/backend/application/CreateLectureUseCase.java)
Hardened lecture creation to prevent client-controlled identity.
 - Updated non-trivial method `createLecture(String title)` to always generate a server-side UUID via `UUID.randomUUID()`.
 - Keeps ownership attribution through `CurrentUserService.requireUserId()` and persists metadata (`createdByInstructorId`, `createdAt`) in the `Lecture` aggregate.

[LectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureController.java)
Aligned create API contract with the server-generated ID policy.
 - Simplified DTO `CreateLectureRequestDTO` to accept only `title`.
 - Updated `createLecture(...)` endpoint to call `CreateLectureUseCase.createLecture(title)` without trusting client-provided lecture IDs.

[SubmissionRepository](backend/src/main/java/com/livequiz/backend/domain/submission/SubmissionRepository.java)
Extended repository contract for batched per-question history queries.
 - Added non-trivial method `findByLectureAndQuestion(LectureId, QuestionId)` returning all submissions for a lecture-question pair.
 - This supports aggregate computation in the application layer and avoids per-student query loops.

[InMemorySubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemorySubmissionRepository.java)
Implemented in-memory support for the new batch submission lookup.
 - Added method `findByLectureAndQuestion(...)` using stream filters over lecture and question IDs.
 - Preserved existing behavior for latest submission lookup and analytics-related methods.

[JpaSubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaSubmissionRepository.java)
Added JPA data access method for lecture-question scoped submission reads.
 - Introduced `findByLectureIdAndQuestionId(String lectureId, String questionId)`.
 - This method is consumed by the Postgres adapter for bulk loading before in-memory grouping.

[JpaPostgresSubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresSubmissionRepository.java)
Implemented domain mapping for the new bulk-read repository contract.
 - Added non-trivial method `findByLectureAndQuestion(...)` that maps `SubmissionEntity` rows to domain `Submission` objects.
 - Keeps existing projection-based analytics and point lookups intact.

[GetQuestionStudentAnswerHistoryUseCase](backend/src/main/java/com/livequiz/backend/application/GetQuestionStudentAnswerHistoryUseCase.java)
Refactored per-student answer history computation to reduce query amplification.
 - Non-trivial method `execute(String lectureId, String questionId)` now preloads all submissions for the question once and groups by student via `Collectors.groupingBy(Submission::studentId)`.
 - Computes `attemptCount` and `latestSubmission` from grouped data with `Comparator.comparing(Submission::timestamp)`.
 - Retains validation flow through `InstructorLectureAccessService` and question existence checks.

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Expanded integration coverage for the hardened create flow and legacy ownership behavior.
 - Added non-trivial test `should_ignore_client_supplied_lecture_id_on_create()` to verify create calls cannot override an existing lecture by supplying `lectureId`.
 - Added non-trivial test `should_reject_access_to_legacy_unowned_lecture_without_claiming()` to verify unowned legacy lectures are rejected with `LECTURE_NOT_FOUND` and are not auto-claimed.
 - Reuses existing helper methods (`createLecture`, `extractField`, auth helpers) to keep scenario setup consistent.
