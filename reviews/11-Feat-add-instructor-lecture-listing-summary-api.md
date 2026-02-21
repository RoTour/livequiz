[ROADMAP](ROADMAP.md)
Updated Phase 7 progress tracking after delivering the instructor lecture list API slice.
 - Marked `Iteration 03 - Instructor lecture listing API (summary read model)` as complete.

[ListInstructorLecturesUseCase](backend/src/main/java/com/livequiz/backend/application/ListInstructorLecturesUseCase.java)
Added a dedicated application read use case for instructor-scoped lecture summaries.
 - `execute()` resolves the authenticated user with `CurrentUserService.requireUserId()`.
 - `execute()` queries `LectureRepository.findByCreatedByInstructorId(...)` and maps to `LectureSummary` records.
 - `LectureSummary` includes non-trivial list rendering fields: `lectureId`, `title`, `createdAt`, `questionCount`, and `unlockedCount`.

[LectureRepository](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureRepository.java)
Extended domain repository contract with ownership-based read path.
 - Added `findByCreatedByInstructorId(String createdByInstructorId)` to support instructor-scoped lecture listing.

[InMemoryLectureRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryLectureRepository.java)
Implemented in-memory ownership query behavior.
 - Added `findByCreatedByInstructorId(...)` filtering by lecture owner metadata.
 - Added ordering by `Lecture.createdAt()` descending with null-safe comparator to keep list presentation stable.

[JpaLectureRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaLectureRepository.java)
Extended Spring Data repository with summary-list backing query.
 - Added `findByCreatedByInstructorIdOrderByCreatedAtDesc(...)` for owner-scoped and recency-ordered lecture retrieval.

[JpaPostgresLectureRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureRepository.java)
Implemented Postgres-side mapping for the new listing contract.
 - Added `findByCreatedByInstructorId(...)` implementation using the new JPA query method.
 - Introduced non-trivial helper `toDomain(LectureEntity entity)` to centralize entity-to-domain reconstruction.
 - Reused `toDomain(...)` in both `findById(...)` and list queries to keep mappings consistent.

[LectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureController.java)
Added instructor listing endpoint while preserving existing create/mutation APIs.
 - Injected `ListInstructorLecturesUseCase` as controller dependency.
 - Added `listInstructorLectures()` mapped to `GET /api/lectures`.
 - Added `InstructorLectureSummaryResponse` DTO and explicit mapping from use case records, including `createdAt` serialization as ISO string.

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Expanded integration coverage for listing API security and response contract.
 - `should_reject_student_for_instructor_only_endpoints()` now verifies students are forbidden from `GET /api/lectures`.
 - `should_require_authentication_for_protected_endpoints()` now includes unauthenticated list access denial.
 - Added `should_list_instructor_lectures_with_summary_fields()` to verify summary fields and computed counts.
 - Added helper methods `createLecture(...)` and `addQuestion(...)` to keep list contract tests readable and reusable.
