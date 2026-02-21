[ROADMAP.md](ROADMAP.md)
Marked Phase 6 Step 8 as completed.
 - Updated the Step 8 checklist item from unchecked to checked to reflect that behavior-focused frontend tests were implemented.

[frontend/stryker.conf.mjs](frontend/stryker.conf.mjs)
Stabilized mutation testing execution strategy.
 - Added `concurrency: 1` to prevent oversubscription between checker and test-runner workers.
 - Kept `testRunner: 'command'` with TypeScript checker, but switched command execution to Node via `./scripts/with-node.sh`.
 - This directly addressed prior high timeout behavior during full mutation runs.

[frontend/src/app/app.routes.spec.ts](frontend/src/app/app.routes.spec.ts)
Added route-level behavior tests for role-protected and fallback routes.
 - Verifies `instructor` route uses both `authGuard` and `instructorGuard`.
 - Verifies `student` and `student/join/:token` routes use `authGuard` and `studentGuard`.
 - Verifies fallback redirects for `dashboard`, root path, and wildcard route.

[frontend/src/app/instructor/components/create-lecture-panel/create-lecture-panel.spec.ts](frontend/src/app/instructor/components/create-lecture-panel/create-lecture-panel.spec.ts)
Added unit tests for `CreateLecturePanel` event and state behavior.
 - Confirms `createLecture` output event emits on valid form submission.
 - Confirms submit button disable behavior for invalid form and rendering of selected lecture id.

[frontend/src/app/instructor/components/question-flow-panel/question-flow-panel.spec.ts](frontend/src/app/instructor/components/question-flow-panel/question-flow-panel.spec.ts)
Added interaction tests for `QuestionFlowPanel` action wiring.
 - Confirms `addQuestion`, `unlockNext`, and `refreshState` outputs emit from UI actions.
 - Verifies control disable behavior when `selectedLectureId` is empty.
 - Introduced `findButton(...)` helper to robustly target labeled action buttons.

[frontend/src/app/instructor/components/invite-management-panel/invite-management-panel.spec.ts](frontend/src/app/instructor/components/invite-management-panel/invite-management-panel.spec.ts)
Added behavior tests for invite management state rendering and actions.
 - Confirms top-level `createInvite` and `refreshInvites` output events emit.
 - Exercises non-trivial invite states (active/revoked/expired) and validates corresponding revoke-button enabled/disabled behavior.
 - Confirms only active invite triggers `revokeInvite` output.
 - Uses a `findButton(...)` helper for stable action targeting.

[frontend/src/app/instructor/components/lecture-state-panel/lecture-state-panel.spec.ts](frontend/src/app/instructor/components/lecture-state-panel/lecture-state-panel.spec.ts)
Added lecture state display and unlock action tests.
 - Verifies empty state message when no lecture data is provided.
 - Verifies mixed locked/unlocked question rendering and non-trivial unlock flow via `unlockQuestion` output event.

[frontend/src/app/student/components/join-lecture-panel/join-lecture-panel.spec.ts](frontend/src/app/student/components/join-lecture-panel/join-lecture-panel.spec.ts)
Added tests for join panel submission and result rendering.
 - Confirms `joinLecture` output event emits on valid invite code submission.
 - Verifies selected lecture id and join-result message rendering.

[frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.spec.ts](frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.spec.ts)
Added tests for answer flow panel control and state transitions.
 - Confirms `loadNextQuestion` event wiring and disabled-state behavior when no lecture is selected.
 - Verifies non-trivial question-present state (prompt + submit form) and `submitAnswer` event emission.
 - Verifies empty-question and cooldown render states.
 - Uses `findButton(...)` helper for button lookup by label.

[plans/1-Student-and-Instructor-workflow.md](plans/1-Student-and-Instructor-workflow.md)
Added a comprehensive phased delivery plan for Phase 7 workflow completion.
 - Defines 16 vertical iterations spanning backend ownership/listing/analytics and frontend route/list/room migration.
 - Includes explicit non-break guarantees and DoD/validation cadence.
 - Highlights non-trivial planned backend and frontend classes/modules (e.g., `CreateLectureUseCase`, `LectureController`, `SecurityConfig`, `instructor-workspace.service.ts`, `student-join-token.ts`) as future touchpoints.
