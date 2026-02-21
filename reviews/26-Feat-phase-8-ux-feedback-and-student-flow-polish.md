[ROADMAP](ROADMAP.md)
Added a new delivery phase for UX polish and clarified persistence work.
 - Added Phase 8 tasks for answer statuses, in-memory seed data, waiting-room refresh strategy, shared date formatting, and toast notifications.
 - Expanded Phase 5 with explicit Postgres profile/repository wiring verification.

[SubmitAnswerUseCase](backend/src/main/java/com/livequiz/backend/application/SubmitAnswerUseCase.java)
Extended submission response shape and kept student submissions in an "awaiting evaluation" state by default.
 - `SubmitResult` now includes `answerStatus` so frontend can render immediate status chips.
 - `execute(...)` now returns `AnswerEvaluationStatus.AWAITING_EVALUATION` after saving.
 - Guard methods `ensureStudentIsEnrolled(...)`, `ensureQuestionIsUnlocked(...)`, and `enforceCooldown(...)` continue to enforce enrollment, lock state, and cooldown invariants.

[AnswerEvaluationStatus](backend/src/main/java/com/livequiz/backend/application/AnswerEvaluationStatus.java)
Introduced a shared status enum for student answer lifecycle.
 - Defines `AWAITING_EVALUATION`, `CORRECT`, `INCORRECT`, `INCOMPLETE` for API and UI contract consistency.

[GetStudentAnswerStatusesUseCase](backend/src/main/java/com/livequiz/backend/application/GetStudentAnswerStatusesUseCase.java)
Added a dedicated use case for retrieving per-question latest answer statuses for the current student.
 - `execute(...)` validates lecture existence, enrollment, sorts questions by order, and maps latest submissions to response records.
 - Uses `AnswerStatusResult` record as an application-layer DTO.
 - Currently reports `AWAITING_EVALUATION` for returned submissions.

[InMemoryDataSeeder](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryDataSeeder.java)
Added profile-specific seed data for faster local/demo startup.
 - `@Profile({ "in-memory", "memory" })` and `@PostConstruct` gate seeding to in-memory runtime only.
 - `seedDefaultLecture()` creates one lecture and one unlocked default question when not already present.

[SecurityConfig](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Updated security route allow-list for new student endpoint.
 - Added `/api/lectures/*/students/me/answer-statuses` to the student/instructor matcher block.
 - Keeps endpoint authorization explicit and aligned with existing student flow endpoints.

[StudentLectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/StudentLectureController.java)
Extended student API surface for answer status reads.
 - Injects `GetStudentAnswerStatusesUseCase`.
 - Adds `StudentAnswerStatusResponse` record.
 - Adds `getAnswerStatuses(...)` endpoint to return question prompt/order, status, and submission timestamp.

[StudentFlowIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java)
Expanded integration coverage for new submission/status contracts.
 - `should_join_with_invite_and_submit_with_cooldown()` now asserts `answerStatus` exists on submission response.
 - Added assertion of `GET /students/me/answer-statuses` to verify status list endpoint behavior.

[App](frontend/src/app/app.ts)
Registered global toast rendering at app shell level.
 - Imports `ToastCenter` in standalone component metadata.

[App Template](frontend/src/app/app.html)
Enabled global toast container in root template.
 - Adds `<app-toast-center/>` near top-level shell so notifications are visible across routes.

[Dashboard](frontend/src/app/dashboard/dashboard.ts)
Adopted shared date formatting utility in legacy dashboard view.
 - Imports `HumanDatePipe` in component `imports` to normalize date rendering.

[Dashboard Template](frontend/src/app/dashboard/dashboard.html)
Normalized invite expiration display.
 - Applies `| humanDate` to `generatedInvite.expiresAt`.

[InviteManagementPanel](frontend/src/app/instructor/components/invite-management-panel/invite-management-panel.ts)
Enabled shared date formatting in instructor invite UI.
 - Imports `HumanDatePipe` for standalone template usage.
 - Non-trivial helper `inviteDisplayStatus(...)` continues mapping active/revoked/expired label states.

[InviteManagementPanel Template](frontend/src/app/instructor/components/invite-management-panel/invite-management-panel.html)
Improved invite metadata readability.
 - Applies `| humanDate` to invite expiration timestamp.

[LectureService](frontend/src/app/lecture.service.ts)
Extended client API contracts for status tracking.
 - Added `getAnswerStatuses(lectureId)` API method.
 - Extended `SubmitAnswerResponse` with `answerStatus`.
 - Added `StudentAnswerStatus` and `StudentAnswerStatusResponse` types used across student workspace components.

[StudentWorkspaceService](frontend/src/app/student/application/student-workspace.service.ts)
Added workspace-level method to load status history.
 - New `getAnswerStatuses(lectureId)` wraps `LectureService.getAnswerStatuses(...)` via `firstValueFrom`.

[AnswerFlowPanel](frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.ts)
Expanded student answer panel inputs and rendering helpers.
 - Added required inputs: `answerStatuses` and `manualReloadDisabled`.
 - Uses `HumanDatePipe` for timestamp display.
 - `answerStatusLabel(...)` converts enum values into user-facing text.

[AnswerFlowPanel Template](frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.html)
Added status history display and reload UX constraints.
 - Reload button now reflects debounce lock state (`Refresh cooling down...`).
 - Added "Your answer statuses" list with per-question chip color mapping and formatted submission timestamps.

[AnswerFlowPanel Spec](frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.spec.ts)
Updated tests for new required component inputs and updated button label.
 - Injects `answerStatuses` and `manualReloadDisabled` in setup.
 - Validates event emission with `Reload next question` label.

[StudentHome](frontend/src/app/student/student-home.ts)
Kept compatibility with updated `AnswerFlowPanel` required inputs.
 - Added state holders `answerStatuses` and `manualReloadDisabled` so template compiles without changing legacy home behavior.

[StudentHome Template](frontend/src/app/student/student-home.html)
Wired new panel inputs required by `AnswerFlowPanel`.
 - Passes `answerStatuses()` and `manualReloadDisabled()` bindings.

[StudentLectureList](frontend/src/app/student/student-lecture-list.ts)
Added human-readable dates and toast feedback on join/list actions.
 - Imports `HumanDatePipe` and `ToastService`.
 - `refreshLectures()` now emits error toast on load failure.
 - `joinLecture()` now emits success/warning toast for enrollment outcomes.

[StudentLectureList Template](frontend/src/app/student/student-lecture-list.html)
Improved lecture date readability.
 - Applies `| humanDate` for `lecture.enrolledAt`.

[StudentLectureRoom Template](frontend/src/app/student/student-lecture-room.html)
Connected room container to expanded answer panel contract.
 - Passes `answerStatuses`, `manualReloadDisabled`, and maps reload action to `manualReload()`.

[StudentLectureRoom Spec](frontend/src/app/student/student-lecture-room.spec.ts)
Extended unit setup to match new room dependencies and behavior.
 - Added `getAnswerStatuses` mock and wired provider.
 - Updated first test to explicitly call `loadNextQuestion()` before asserting rendered prompt.

[StudentLectureRoom](frontend/src/app/student/student-lecture-room.ts)
Implemented core waiting-room UX improvements and status loading behavior.
 - Adds debounced manual reload via `manualReload()` and `MANUAL_RELOAD_COOLDOWN_MS`.
 - Adds auto-polling while no question is available via `configurePolling(...)` with `POLL_INTERVAL_MS`.
 - Stops timers defensively in `loadLectureContext(...)` and `ngOnDestroy()` using `stopPolling()`.
 - Loads status history with `loadAnswerStatuses(...)` and displays toast notifications for major outcomes.
 - `submitAnswer()` now surfaces backend `answerStatus` in a success toast.
 - `loadNextQuestionWithOptions(...)` centralizes next-question fetch, status line updates, and enrollment/error handling.

[Global Styles](frontend/src/styles.css)
Added reusable UI tokens/styles to support status chips and toast system.
 - Introduced `.lq-chip-danger` and `.lq-chip-info` for extended answer status visuals.
 - Added toast layout and variants: `.lq-toast-center`, `.lq-toast`, `.lq-toast-info|success|warning|error`, `.lq-toast-close`.

[HumanDatePipe](frontend/src/app/shared/date/human-date.pipe.ts)
Added reusable date formatting pipe for consistent human-readable timestamps.
 - `transform(...)` handles null/invalid input safely and formats with `Intl.DateTimeFormat`.

[ToastService](frontend/src/app/shared/toast/toast.service.ts)
Introduced app-wide transient notification state manager.
 - Uses Angular `signal` store for reactive toast list.
 - `show(...)` creates toast IDs and auto-dismisses after configurable duration.
 - `dismiss(...)` removes specific messages.

[ToastCenter](frontend/src/app/shared/toast/toast-center.ts)
Added presentation component for rendering queued toasts.
 - Injects `ToastService` and exposes `dismiss(id)` action.

[ToastCenter Template](frontend/src/app/shared/toast/toast-center.html)
Implemented toast list rendering and manual dismiss affordance.
 - Renders each toast with level-aware CSS class binding.
 - Adds accessible dismiss button and polite ARIA live region.
