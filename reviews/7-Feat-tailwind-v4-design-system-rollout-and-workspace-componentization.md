[Roadmap Step 7](ROADMAP.md)
Marked Phase 6 Step 7 as completed.
 - Updated the checklist entry for cohesive visual system rollout from pending to done.

[Global frontend styles](frontend/src/styles.css)
Introduced the Tailwind v4 tokenized design system as the single source of truth.
 - Added `@theme` design tokens (`--color-lq-*`, `--font-sans`) so the app can stay config-free without `tailwind.config.*`.
 - Added shared primitives (`.lq-page`, `.lq-card`, `.lq-btn`, `.lq-input`, `.lq-chip`, `.lq-alert`) used by all updated pages/components.
 - Added non-trivial motion and responsive behavior via keyframes (`@keyframes lq-rise`) and mobile layout overrides.

[App shell template](frontend/src/app/app.html)
Migrated top-level shell and navigation to shared design-system classes.
 - Replaced ad-hoc utility color classes with `lq-*` primitives for brand, role chip, navigation actions, and logout action.
 - Kept route-role behavior intact while making active navigation state use `routerLinkActive="lq-btn-primary"`.

[Legacy dashboard template](frontend/src/app/dashboard/dashboard.html)
Restyled the end-to-end demo/control dashboard to the new design language.
 - Replaced previous mixed utility classes with `lq-*` cards, buttons, chips, and alerts.
 - Preserved existing form/event bindings and feature flow (create, add, unlock, invite, join, submit, state).

[Design system showcase template](frontend/src/app/design-system/design-system.html)
Converted showcase page to consume the same shared primitives used in production routes.
 - Replaced standalone `ds-*` showcase structure with `lq-*` token-driven examples for buttons, controls, chips, and alerts.

[Design system showcase stylesheet](frontend/src/app/design-system/design-system.css)
Simplified component-local styling after global token migration.
 - Removed old local token system and now only keeps `:host { display: block; }`.

[Instructor workspace container](frontend/src/app/instructor/instructor-home.ts)
Refactored container to compose dedicated section components while preserving orchestration logic.
 - Added section component imports/classes: `CreateLecturePanel`, `QuestionFlowPanel`, `InviteManagementPanel`, `LectureStatePanel`.
 - Kept non-trivial orchestration methods in `InstructorHome`: `createLecture()`, `addQuestion()`, `unlockNextQuestion()`, `unlockQuestion()`, `refreshLectureState()`, `createInvite()`, `revokeInvite()`, `refreshInvites()`.
 - Removed local invite-status presentational helper from container and delegated display concerns to panel component.

[Instructor workspace template](frontend/src/app/instructor/instructor-home.html)
Split monolithic page markup into dedicated section components.
 - Wired state/form inputs and mutation outputs to specialized panels.
 - Preserved all existing actions and signal-driven status presentation.

[Instructor create lecture panel class](frontend/src/app/instructor/components/create-lecture-panel/create-lecture-panel.ts)
Added a focused presentational section component for lecture creation.
 - Introduced `CreateLecturePanel` class with non-trivial event bridge method `submit()` that emits `createLecture`.

[Instructor create lecture panel template](frontend/src/app/instructor/components/create-lecture-panel/create-lecture-panel.html)
Added reusable panel template for create-lecture form section.
 - Displays selected lecture id and binds to parent `FormGroup` via input contract.

[Instructor question flow panel class](frontend/src/app/instructor/components/question-flow-panel/question-flow-panel.ts)
Added dedicated component for add/unlock/refresh actions.
 - Introduced `QuestionFlowPanel` class with non-trivial output bridge methods `submit()`, `unlockNextQuestion()`, and `refreshLectureState()`.

[Instructor question flow panel template](frontend/src/app/instructor/components/question-flow-panel/question-flow-panel.html)
Added template for question mutation controls.
 - Includes guarded actions tied to selected lecture presence and form validity.

[Instructor invite management panel class](frontend/src/app/instructor/components/invite-management-panel/invite-management-panel.ts)
Added invite-focused presentational component.
 - Introduced `InviteManagementPanel` class with non-trivial methods `create()`, `refresh()`, `revoke(inviteId)`, and `inviteDisplayStatus(invite)`.
 - Centralizes invite status derivation (`active` / `revoked` / `expired`) at component boundary.

[Instructor invite management panel template](frontend/src/app/instructor/components/invite-management-panel/invite-management-panel.html)
Added invite list/status UI section using shared primitives.
 - Renders generated invite summary, list with status chips, and revoke controls.

[Instructor lecture state panel class](frontend/src/app/instructor/components/lecture-state-panel/lecture-state-panel.ts)
Added dedicated lecture-state panel component.
 - Introduced `LectureStatePanel` class with non-trivial `unlock(questionId)` event handoff to container.

[Instructor lecture state panel template](frontend/src/app/instructor/components/lecture-state-panel/lecture-state-panel.html)
Added state visualization panel for questions and unlock actions.
 - Uses status chips for locked/unlocked and emits unlock action from each row.

[Login template](frontend/src/app/login/login.html)
Migrated authentication form UI to design-system primitives.
 - Preserved existing reactive form behavior and validation messages.
 - Replaced color/spacing utilities with systemized `lq-*` classes.

[Backend status class](frontend/src/app/shared/backend-status/backend-status.ts)
Aligned dev status bar style mapping with the new design system.
 - Kept `BackendStatus` class behavior and non-trivial timer-driven `constructor()` logic unchanged.
 - Updated non-trivial computed style selector `bgClass` to use `lq-dev-ok` / `lq-dev-down` classes.

[Backend status template](frontend/src/app/shared/backend-status/backend-status.html)
Updated status strip styling to tokenized colors and border tokens.
 - Preserved conditional rendering and text semantics.

[Student workspace container](frontend/src/app/student/student-home.ts)
Refactored student container into section components while preserving core behavior.
 - Added section component imports/classes: `JoinLecturePanel`, `AnswerFlowPanel`.
 - Kept non-trivial orchestration methods in `StudentHome`: `ngOnInit()`, `joinLecture()`, `loadNextQuestion()`, `submitAnswer()`.
 - Maintained deep-link hydration, enrollment handling, and cooldown error mapping logic.

[Student workspace template](frontend/src/app/student/student-home.html)
Split monolithic student page into dedicated panels.
 - Wires join and answer panels via explicit inputs/outputs and shared status header.

[Student join lecture panel class](frontend/src/app/student/components/join-lecture-panel/join-lecture-panel.ts)
Added a dedicated join section component.
 - Introduced `JoinLecturePanel` class with non-trivial `submit()` output bridge method.

[Student join lecture panel template](frontend/src/app/student/components/join-lecture-panel/join-lecture-panel.html)
Added reusable join form template.
 - Displays selected lecture and join result from parent signals.

[Student answer flow panel class](frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.ts)
Added dedicated answer-flow presentational component.
 - Introduced `AnswerFlowPanel` class with non-trivial bridge methods `loadNext()` and `submit()`.

[Student answer flow panel template](frontend/src/app/student/components/answer-flow-panel/answer-flow-panel.html)
Added reusable answer-flow template.
 - Renders question state, submit form, and cooldown feedback using design-system primitives.

[Student token-join template](frontend/src/app/student/student-join-token.html)
Migrated token join status screen to the unified design system.
 - Preserved success/error messaging behavior while replacing legacy utility styling with `lq-*` classes.
