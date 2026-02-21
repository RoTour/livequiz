[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking to reflect Iteration 01 completion and keep the iteration checklist visible.
 - Marked `Iteration 01 - Baseline contract lock (no feature change)` as complete (`[x]`).
 - Preserved the 16-iteration structure so the roadmap remains aligned with `plans/1-Student-and-Instructor-workflow.md`.

[HealthController](backend/src/main/java/com/livequiz/backend/controllers/HealthController.java)
Expanded health endpoint mapping to support both legacy and API-prefixed paths.
 - `health()` now uses `@GetMapping({ "/health", "/api/health" })` so both URLs return the same status payload.
 - Kept response contract stable (`status`, `timestamp`) while widening compatibility.

[SecurityConfig](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Adjusted security policy to keep health checks public across both paths.
 - In `securityFilterChain(...)`, updated `requestMatchers` to permit `"/health"` and `"/api/health"` in addition to auth endpoints.
 - Maintained existing role-based restrictions for instructor and student lecture operations.

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Hardened backend API/security contract coverage for public and protected routes.
 - Added helper `loginAsStudent()` to create a student JWT for role-based authorization tests.
 - Added `should_keep_healthcheck_public_without_authentication()` to lock unauthenticated access for `/health` and `/api/health`.
 - Added `should_reject_student_for_instructor_only_endpoints()` to verify student role receives `403` on instructor-only mutations.
 - Added `should_require_authentication_for_protected_endpoints()` to verify unauthenticated access is denied.
 - Reused `extractField(...)` for parsing `lectureId` from create-lecture responses in follow-up assertions.

[StudentFlowIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java)
Expanded end-to-end student/invite/submission integration coverage for Iteration 01 baseline lock.
 - Added `should_require_invite_credentials_when_joining()` to assert `INVITE_CREDENTIALS_REQUIRED` for empty join payloads.
 - Added `should_require_enrollment_before_next_question_and_submission()` to lock enrollment guard behavior on read/write student flows.
 - Added `should_reject_submission_for_locked_or_unknown_question()` to verify `QUESTION_LOCKED` and `QUESTION_NOT_FOUND` error contracts.
 - Added `should_join_with_invite_token_idempotently()` to validate token join path and `alreadyEnrolled`/`enrolledAt` idempotency semantics.
 - Added helpers `joinLectureByCode(...)`, `joinLectureByToken(...)`, and `extractInviteToken(...)` to reduce duplication in join-path tests.
 - Enhanced `extractField(...)` to parse both quoted JSON strings and unquoted values (e.g., booleans), enabling robust assertions across response types.

[app.routes.spec](frontend/src/app/app.routes.spec.ts)
Locked route-to-component wiring in addition to guard checks.
 - Added component assertions for `instructor`, `student`, and `student/join/:token` routes.
 - Kept guard contract assertions (`authGuard`, `instructorGuard`, `studentGuard`) to prevent accidental route protection regressions.

[instructor-home.spec](frontend/src/app/instructor/instructor-home.spec.ts)
Added failure-path behavior tests for instructor workspace actions.
 - Added create-lecture failure assertion to verify actionable status messaging and prevent refresh side effects.
 - Added add-question failure assertion to ensure error reporting without unnecessary lecture-state refresh.
 - Added `refreshInvites()` failure and `refreshLectureState()` failure assertions to lock retry messaging UX.
 - Continued to use `setupLecture(...)` helper to establish lecture context before mutation and refresh-path assertions.

[student-home.spec](frontend/src/app/student/student-home.spec.ts)
Expanded student workflow UI-contract coverage for error handling and no-op guards.
 - Added join failure case for explicit status feedback (`Could not join lecture...`).
 - Added enrollment-required handling case to lock `LECTURE_ENROLLMENT_REQUIRED` messaging.
 - Added generic submit failure case to lock fallback error messaging when failures are not cooldown-related.
 - Added no-selected-lecture guard case to verify `loadNextQuestion()` does not call service prematurely.
