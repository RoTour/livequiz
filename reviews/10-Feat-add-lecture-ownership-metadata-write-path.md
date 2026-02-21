[ROADMAP](ROADMAP.md)
Updated Phase 7 progress tracking for delivery visibility.
 - Marked `Iteration 02 - Lecture ownership metadata write path` as complete after backend implementation and validation.

[CreateLectureUseCase](backend/src/main/java/com/livequiz/backend/application/CreateLectureUseCase.java)
Extended lecture creation flow to persist instructor ownership metadata.
 - `createLecture(...)` now resolves the authenticated instructor identity through `CurrentUserService.requireUserId()`.
 - `createLecture(...)` now builds `Lecture` with `createdByInstructorId` and `createdAt` (`Instant.now()`), keeping the API response contract unchanged (`lectureId` only).
 - Constructor wiring now injects `CurrentUserService` alongside `LectureRepository`.

[Lecture](backend/src/main/java/com/livequiz/backend/domain/lecture/Lecture.java)
Extended domain aggregate model with backward-tolerant ownership metadata.
 - Added `createdByInstructorId` and `createdAt` fields with accessors `createdByInstructorId()` and `createdAt()`.
 - Added overloaded constructors for metadata-aware creation while preserving legacy constructor signatures used in existing code paths.
 - Added constructor invariant enforcing all-or-nothing ownership metadata (both fields present or both absent), plus blank owner validation.
 - Updated mutation methods `addQuestion(...)` and `unlockQuestion(...)` to preserve metadata when returning new immutable `Lecture` instances.

[LectureEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEntity.java)
Extended JPA persistence model to store ownership metadata.
 - Added fields `createdByInstructorId` and `createdAt`.
 - Added new constructor including metadata while retaining existing constructors for compatibility.
 - Added getters `getCreatedByInstructorId()` and `getCreatedAt()` for repository mapping.

[JpaPostgresLectureRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureRepository.java)
Updated persistence mapping to write/read lecture ownership metadata.
 - In `save(...)`, now maps `Lecture.createdByInstructorId()` and `Lecture.createdAt()` into `LectureEntity`.
 - In `findById(...)`, now reconstructs `Lecture` with ownership metadata from entity fields.
 - Mapping remains tolerant of legacy rows because `Lecture` allows both metadata fields to be null together.

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Added integration test coverage for metadata write-path behavior.
 - Added dependency on `LectureRepository` for verifying persisted aggregate fields after API-driven lecture creation.
 - Added `should_store_lecture_ownership_metadata_on_create()` to verify `createdByInstructorId` is set to authenticated instructor and `createdAt` is non-null.
 - Reused helper `extractField(...)` to parse response payload for persisted lecture lookup.

[LectureTest](backend/src/test/java/com/livequiz/backend/domain/lecture/LectureTest.java)
Added domain-level regression coverage for metadata immutability behavior.
 - Added `should_preserve_ownership_metadata_across_mutations()` to verify metadata survives `addQuestion(...)` and `unlockQuestion(...)` transitions.
