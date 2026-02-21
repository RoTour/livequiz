[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing instructor ownership enforcement.
 - Marked `Iteration 07 - Instructor ownership enforcement on existing endpoints` as complete.

[InstructorLectureAccessService](backend/src/main/java/com/livequiz/backend/application/InstructorLectureAccessService.java)
Added centralized application-layer ownership guard for instructor lecture access.
 - Introduced non-trivial method `getOwnedLectureOrThrow(...)` to load lecture and validate it belongs to the authenticated instructor from `CurrentUserService`.
 - Enforces not-found policy (`LECTURE_NOT_FOUND`) for non-owners to avoid resource disclosure.

[Instructor lecture mutation/query use cases](backend/src/main/java/com/livequiz/backend/application/AddQuestionToLectureUseCase.java)
Applied ownership guard across existing instructor lecture operations.
 - `AddQuestionToLectureUseCase`, `UnlockQuestionUseCase`, `UnlockNextQuestionUseCase`, and `GetLectureStateUseCase` now resolve lecture access through `InstructorLectureAccessService`.
 - `CreateLectureInviteUseCase`, `ListLectureInvitesUseCase`, and `RevokeLectureInviteUseCase` now validate lecture ownership before invite operations.

[SecurityConfig](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Extended in-memory test users with a second instructor account.
 - Added `instructor2` to enable cross-instructor authorization integration testing.

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Expanded integration coverage for ownership policy behavior.
 - Added `should_enforce_instructor_ownership_on_lecture_resources` to verify non-owner access to state, question mutations, and invite management returns `404 LECTURE_NOT_FOUND`.
 - Added `should_list_only_owned_lectures_per_instructor` to verify instructor lecture list is owner-scoped.
 - Added helper methods `loginAsSecondInstructor()` and `createInvite(...)` to support multi-instructor test scenarios.
