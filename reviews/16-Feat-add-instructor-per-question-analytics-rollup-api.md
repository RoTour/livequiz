[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing instructor per-question analytics rollup.
 - Marked `Iteration 08 - Instructor per-question analytics rollup API` as complete.

[GetLectureQuestionAnalyticsUseCase](backend/src/main/java/com/livequiz/backend/application/GetLectureQuestionAnalyticsUseCase.java)
Added a dedicated analytics query use case for instructor lecture detail monitoring.
 - Introduced non-trivial method `execute(...)` to aggregate question-level metrics for an owned lecture.
 - Computes per-question rollup fields: `enrolledCount`, `answeredCount`, `unansweredCount`, and `multiAttemptCount`.
 - Reuses `InstructorLectureAccessService` so analytics follows the same ownership policy as other instructor endpoints.

[LectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureController.java)
Added an additive instructor analytics endpoint.
 - New `GET /api/lectures/{lectureId}/questions/analytics` endpoint returns rollup rows keyed by question.
 - Added response DTO `QuestionAnalyticsResponse` for explicit API contract mapping.

[Repository query extensions](backend/src/main/java/com/livequiz/backend/domain/submission/SubmissionRepository.java)
Extended domain repository contracts and persistence adapters for analytics reads.
 - `LectureEnrollmentRepository` now provides lecture enrollment count lookup.
 - `SubmissionRepository` now exposes per-question per-student attempt snapshots for aggregation.
 - Implemented in in-memory and JPA adapters (`InMemoryLectureEnrollmentRepository`, `InMemorySubmissionRepository`, `JpaPostgresLectureEnrollmentRepository`, `JpaPostgresSubmissionRepository`) and JPA interfaces (`JpaLectureEnrollmentRepository`, `JpaSubmissionRepository`).

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Expanded integration coverage for analytics behavior and ownership scope.
 - Added `should_return_per_question_analytics_rollup_for_owned_lecture` to validate rollup values for answered/unanswered and multi-attempt scenarios.
 - Extended ownership test to assert non-owner access to analytics endpoint returns `404 LECTURE_NOT_FOUND`.
