[Roadmap](ROADMAP.md)
Updated rollout progress and linked the active UX implementation plan.
 - Added a `Related plan` pointer to `plans/2-QR-invite-autojoin-and-anonymous-student-onboarding-plan.md`.
 - Marked Phase 6 Step 4 (invite management) and Step 5 (student workspace migration) as completed.

[Frontend gitignore](frontend/.gitignore)
Extended ignored artifacts for mutation-testing outputs.
 - Added `/.stryker-tmp`, `/reports/mutation`, and `/stryker.log` so generated mutation artifacts do not pollute source control.

[Bun lockfile](frontend/bun.lock)
Lockfile regenerated to pin new testing dependencies.
 - Added resolved entries for `@vitest/coverage-v8` and Stryker packages (`@stryker-mutator/core`, `@stryker-mutator/vitest-runner`, `@stryker-mutator/typescript-checker`) plus their transitive graph.

[Frontend package manifest](frontend/package.json)
Expanded test tooling and scripts for coverage + mutation workflows.
 - Added script `test:coverage` (`ng test --watch=false --coverage`) for deterministic CI-friendly coverage runs.
 - Added script `test:mutation` (`stryker run`) for mutation analysis.
 - Added dev dependencies for Vitest V8 coverage and Stryker mutation tooling.

[InstructorWorkspaceService](frontend/src/app/instructor/application/instructor-workspace.service.ts)
Extended instructor application adapter to support invite lifecycle operations.
 - `InstructorWorkspaceService` remains the non-UI orchestration class wrapping HTTP observables with `firstValueFrom(...)`.
 - Non-trivial methods added: `createInvite(lectureId)`, `listInvites(lectureId)`, and `revokeInvite(lectureId, inviteId)`.

[InstructorHome template](frontend/src/app/instructor/instructor-home.html)
Added full invite management UI to instructor workspace.
 - New section includes actions for generate, refresh, and revoke invite.
 - Displays a highlighted "New code" card from latest invite creation and a list with per-invite status badges.
 - Uses `inviteDisplayStatus(invite)` and active-state disabling to communicate lifecycle state clearly.

[InstructorHome spec](frontend/src/app/instructor/instructor-home.spec.ts)
Expanded component tests for invite workflows and state consistency.
 - Added mocked service functions: `createInvite`, `listInvites`, `revokeInvite`.
 - Non-trivial scenarios added: create invite + list refresh, revoke invite + list refresh, and clearing stale invite summary when creating a new lecture.
 - Existing tests continue to validate create/add/unlock flows and refresh-failure messaging.

[InstructorHome](frontend/src/app/instructor/instructor-home.ts)
Added invite management state + actions and strengthened lecture-switch consistency.
 - `InstructorHome` class now stores `lastCreatedInvite` and `invites` signals.
 - Non-trivial method `createLecture()` now clears prior invite state when switching to a newly created lecture, then refreshes both lecture state and invite list.
 - Non-trivial methods added: `createInvite()`, `revokeInvite(inviteId)`, `refreshInvites(options)` and `inviteDisplayStatus(invite)`.
 - Existing mutation/refresh separation remains in place so mutation success is not misreported when follow-up refresh fails.

[LectureService](frontend/src/app/lecture.service.ts)
Extended frontend lecture API contract for invite listing and revocation.
 - Non-trivial methods added: `listInvites(lectureId)` and `revokeInvite(lectureId, inviteId)`.
 - Added `LectureInviteResponse` exported type to model invite status fields (`createdAt`, `expiresAt`, `revokedAt`, `active`).

[Login template](frontend/src/app/login/login.html)
Adjusted template form control access for strict-safe Angular template typing.
 - Replaced direct `form.controls.username` reads with `form.get('username')?.` access in validation condition.
 - Avoids possible undefined template access under strict template checks.

[BackendStatus template](frontend/src/app/shared/backend-status/backend-status.html)
Updated style binding API to match component refactor.
 - Switched background class interpolation from `styles.bg()` to `bgClass()`.

[BackendStatus](frontend/src/app/shared/backend-status/backend-status.ts)
Simplified computed style state into direct signal-derived class.
 - `BackendStatus` class replaced nested `styles` object with direct non-trivial computed `bgClass`.
 - Maintains existing timer-driven `timeInStatus` behavior and dev-only polling lifecycle.

[StudentHome template](frontend/src/app/student/student-home.html)
Replaced placeholder with complete student join + answer workflow UI.
 - Added join form (6-char code), join-result messaging, and selected lecture display.
 - Added answer-flow section with next-question loading, answer submission textarea, and cooldown feedback.

[StudentHome](frontend/src/app/student/student-home.ts)
Implemented student workspace orchestration with join, fetch-next, submit, and cooldown handling.
 - `StudentHome` class uses reactive forms + signals for join state, question state, and cooldown messaging.
 - Non-trivial method `joinLecture()` now normalizes invite code and resets lecture-bound state (`nextQuestion`, `submitAnswerForm`, `cooldownMessage`) before switching lectures.
 - Non-trivial method `loadNextQuestion()` handles explicit enrollment-required API code path.
 - Non-trivial method `submitAnswer()` handles successful submission chaining and 429 cooldown retry messaging.

[StudentWorkspaceService](frontend/src/app/student/application/student-workspace.service.ts)
Added dedicated student application adapter for lecture interactions.
 - `StudentWorkspaceService` class wraps `LectureService` endpoints with Promise-based methods for component orchestration.
 - Non-trivial methods: `joinLectureByCode`, `getNextQuestion`, and `submitAnswer`.

[StudentHome spec](frontend/src/app/student/student-home.spec.ts)
Added comprehensive student workspace unit coverage.
 - Covers join success, next-question loading, answer submission flow, and cooldown throttling handling.
 - Non-trivial regression case verifies stale question state is cleared when joining a new lecture to prevent cross-lecture submissions.

[Stryker config](frontend/stryker.conf.mjs)
Introduced project mutation-testing configuration.
 - Configures `testRunner: 'command'` with `commandRunner.command` using existing `bun run test -- --watch=false` flow.
 - Uses `tsconfig.app.json`, mutates `src/app/**/*.ts` excluding specs, and emits HTML mutation report to `reports/mutation/mutation.html`.

[QR + Anonymous onboarding plan](plans/2-QR-invite-autojoin-and-anonymous-student-onboarding-plan.md)
Added a structured implementation plan for next rollout slice.
 - Defines context, goals, and phased execution for QR invite UX, public token route behavior, anonymous auth bootstrap, auto-join wiring, and behavior tests.
 - Includes deferred Phase B plan for school-email registration and verification while preserving anonymous student progress continuity.
