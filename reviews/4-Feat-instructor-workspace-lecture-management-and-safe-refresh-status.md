[Roadmap](ROADMAP.md)
Marked frontend rollout progress for completed shell and instructor migration work.
 - Updated Phase 6 checklist to mark Step 2 and Step 3 as done.
 - Captures delivery state so roadmap reflects the implemented role-aware app shell and instructor workspace slice.

[InstructorHome template](frontend/src/app/instructor/instructor-home.html)
Replaced placeholder content with a functional instructor workspace UI.
 - Added a `Create lecture` form section bound to `createLectureForm`.
 - Added `Add question and unlock flow` controls with actions for add question, unlock next, and manual state refresh.
 - Added lecture-state rendering using `@if` and `@for` to list questions and per-question unlock action.
 - Displays live status text and selected lecture id to keep instructor feedback visible.

[InstructorHome](frontend/src/app/instructor/instructor-home.ts)
Converted the component into a full orchestration class for instructor actions.
 - `InstructorHome` now uses reactive state via Angular signals (`status`, `selectedLectureId`, `lectureState`) and reactive forms for create/add flows.
 - Non-trivial method `createLecture()` executes mutation, sets selected lecture context, then refreshes state with separated error handling.
 - Non-trivial method `addQuestion()` submits payload, resets form defaults, and differentiates mutation failure from refresh failure.
 - Non-trivial methods `unlockNextQuestion()` and `unlockQuestion(questionId)` trigger unlock APIs and preserve successful action messaging when refresh cannot be loaded.
 - Non-trivial method `refreshLectureState(options)` now returns `boolean` and supports `preserveStatusOnError` to avoid incorrectly reporting mutation failures when only follow-up sync fails.

[LectureService](frontend/src/app/lecture.service.ts)
Extended lecture API client with targeted unlock endpoint support.
 - Added non-trivial method `unlockQuestion(lectureId, questionId)` that calls `POST /api/lectures/{lectureId}/questions/{questionId}/unlock`.
 - Keeps API surface aligned with instructor UI needs for unlocking a specific question.

[InstructorWorkspaceService](frontend/src/app/instructor/application/instructor-workspace.service.ts)
Added an application-level adapter to decouple instructor component logic from raw HTTP observables.
 - `InstructorWorkspaceService` wraps `LectureService` calls using `firstValueFrom(...)` and exposes Promise-based methods consumed by `InstructorHome`.
 - Non-trivial methods include `createLecture`, `addQuestion`, `unlockNextQuestion`, `unlockQuestion`, and `getLectureState`.
 - Centralizes request orchestration so component methods stay focused on UI state transitions and feedback.

[InstructorHome spec](frontend/src/app/instructor/instructor-home.spec.ts)
Added focused unit tests for instructor workspace behaviors.
 - Provides mocked `InstructorWorkspaceService` methods and validates create/add/unlock workflows.
 - Verifies form validation guard (invalid create form does not call service).
 - Non-trivial regression test covers scenario where lecture creation succeeds but state refresh fails, asserting status message reports success with refresh warning instead of false failure.
