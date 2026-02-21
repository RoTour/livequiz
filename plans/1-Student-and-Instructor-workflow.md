# 1 - Student and Instructor Workflow Plan

## Context

Phase 7 in `ROADMAP.md` aims to move from isolated feature fragments to complete, route-driven classroom workflows.

Current gaps to close:
- instructor flow assumes one active lecture in component state and does not support multi-lecture lifecycle,
- student flow relies on partial route/query-param behavior instead of a complete lecture-room route model,
- instructor analytics and per-question answer history are not yet delivered in the UI.

This plan defines small, mergeable iterations for a No Estimates workflow.

## Delivery Principles

- Every iteration is vertical, releasable, and must not break existing behavior.
- Keep backward compatibility while migrating routes/components (add first, remove later).
- Follow DDD + clean architecture boundaries:
  - Domain: invariants and core rules.
  - Application: use-case orchestration and policy checks.
  - Infrastructure: controllers, DTO mapping, persistence adapters, security mapping.
  - Frontend: routing + component orchestration in UI, API contracts in services.
- WIP limit: one active iteration per lane; finish to green before starting next.

## Iteration Plan

### Iteration 01 - Baseline Contract Lock (No Feature Change)

Goal: increase safety before refactors.

Scope:
- Add/strengthen backend integration tests for current lecture, invite, join, next-question, and submission flows.
- Add/strengthen frontend tests for current route guards and existing instructor/student workspace behavior.

Expected changes:
- `backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java`
- `backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java`
- `frontend/src/app/app.routes.spec.ts`
- `frontend/src/app/instructor/instructor-home.spec.ts`
- `frontend/src/app/student/student-home.spec.ts`

Non-break guarantee:
- Runtime code unchanged; test-only hardening.

### Iteration 02 - Lecture Ownership Metadata Write Path

Goal: make lecture ownership explicit at creation.

Scope:
- Extend lecture model and persistence with `createdByInstructorId` and `createdAt`.
- Populate ownership metadata using authenticated instructor identity during lecture creation.

Expected backend changes:
- Domain lecture model update with backward-tolerant constructors/mapping.
- Create lecture use case gets current user id and writes metadata.
- Persistence adapters/entities map metadata for in-memory and Postgres profiles.

Likely files:
- `backend/src/main/java/com/livequiz/backend/domain/lecture/Lecture.java`
- `backend/src/main/java/com/livequiz/backend/application/CreateLectureUseCase.java`
- `backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureController.java`
- `backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEntity.java`
- `backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureRepository.java`
- `backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryLectureRepository.java`

Non-break guarantee:
- Existing create response contract remains `{ lectureId }`.
- Legacy rows/reads remain tolerant during rollout.

### Iteration 03 - Instructor Lecture Listing API (Summary Read Model)

Goal: provide instructor-scoped lecture listing for list page rendering.

Scope:
- Add read path: "lectures owned by current instructor".
- Add summary contract including `lectureId`, `title`, `createdAt`, `questionCount`, `unlockedCount`.
- Add instructor-only endpoint for lecture list.

Expected backend changes:
- New query/use-case class in application layer.
- Repository query methods in domain interface and persistence adapters.
- Controller endpoint and DTO mapping.
- Security mapping in `SecurityConfig` if endpoint pattern changes.

Non-break guarantee:
- Existing lecture detail/mutation endpoints unchanged.

### Iteration 04 - Instructor Route Scaffold (Backward Compatible)

Goal: activate route structure without moving all logic yet.

Scope:
- Add `/instructor/lectures` and `/instructor/lectures/:lectureId` routes.
- Keep `/instructor` route as redirect to preserve current links/navigation.

Expected frontend changes:
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/app.routes.spec.ts`
- If needed, minor nav wiring in `frontend/src/app/app.html`.

Non-break guarantee:
- Existing `/instructor` entry still works through redirect.

### Iteration 05 - Instructor Lecture List Page

Goal: first complete instructor step after login: list/create/select lecture.

Scope:
- Build lecture list UI with empty state.
- Support create lecture action from list page.
- Navigate to selected lecture detail route.

Expected frontend changes:
- New instructor list component/page.
- Add list API methods in:
  - `frontend/src/app/lecture.service.ts`
  - `frontend/src/app/instructor/application/instructor-workspace.service.ts`
- Tests for empty/list/create/select flow.

Non-break guarantee:
- Legacy instructor detail still works while list page is introduced.

### Iteration 06 - Instructor Detail Route Param Source of Truth

Goal: detail page state comes from route param, not local single-lecture signal.

Scope:
- Refactor instructor detail to load lecture by `:lectureId`.
- Preserve existing behaviors: add question, unlock next/specific, invite generate/list/revoke, state refresh.
- Ensure hard refresh keeps selected lecture context.

Expected frontend changes:
- Refactor current logic from `frontend/src/app/instructor/instructor-home.ts` into route-driven detail page.
- Update templates/tests accordingly.

Non-break guarantee:
- Core instructor actions remain functionally identical.

### Iteration 07 - Instructor Ownership Enforcement on Existing Endpoints

Goal: enforce ownership policy beyond role checks.

Scope:
- Add application-layer policy checks so instructor can only read/mutate their own lecture resources.
- Apply checks to lecture state, question mutations, invite management, and any new list/detail queries.

Expected backend changes:
- Use-case-level authorization guard methods.
- Possible helper service for ownership validation.
- Integration tests with two instructors verifying forbidden/not-found policy outcome.

Non-break guarantee:
- Legit owner behavior remains unchanged.

### Iteration 08 - Instructor Per-Question Analytics Rollup API

Goal: provide class monitoring metrics per question.

Scope:
- Add instructor analytics endpoint for lecture detail.
- Per question rollup fields:
  - `enrolledCount`
  - `answeredCount`
  - `unansweredCount`
  - `multiAttemptCount`

Expected backend changes:
- New application query use case for analytics.
- Repository query extensions for enrollments/submissions.
- DTO + controller endpoint mapping.

Non-break guarantee:
- Analytics endpoint additive; no change to existing submission/progression policy.

### Iteration 09 - Instructor Per-Question Student Answer History API

Goal: provide drilldown visibility for struggling/non-responding students.

Scope:
- Add endpoint returning answer history snapshot per student for one question.
- Fields per student row:
  - `studentId`
  - `latestAnswerAt`
  - `attemptCount`
  - `latestAnswerText` (or preview)

Expected backend changes:
- Application query use case.
- Submission repository query support.
- Controller endpoint + response contract tests.

Non-break guarantee:
- Additive read-only endpoint.

### Iteration 10 - Instructor Analytics UI Integration

Goal: complete instructor journey with monitoring inside lecture detail.

Scope:
- Display question status with analytics metrics in detail UI.
- Add drilldown interaction to open student answer history for selected question.
- Handle loading/empty/error states explicitly.

Expected frontend changes:
- API contract additions in `frontend/src/app/lecture.service.ts`.
- Workspace service methods in `frontend/src/app/instructor/application/instructor-workspace.service.ts`.
- New/extended detail panels and tests.

Non-break guarantee:
- If analytics fails, existing question/invite operations remain usable.

### Iteration 11 - Student Lecture Listing API

Goal: provide student enrolled lecture list for re-entry.

Scope:
- Add endpoint listing lectures for current student.
- Include minimal list metadata (`lectureId`, `title`, enrollment/progress hints as needed).

Expected backend changes:
- New application query use case.
- Enrollment + lecture repository query composition.
- Security matcher update in `SecurityConfig` for student list endpoint.

Non-break guarantee:
- Existing join/next-question/submission endpoints unchanged.

### Iteration 12 - Student Route Scaffold (Backward Compatible)

Goal: establish route model for list and room without breaking current entry.

Scope:
- Add routes:
  - `/student/lectures`
  - `/student/lectures/:lectureId`
- Keep `/student` as redirect.
- Keep `/student/join/:token` and align post-login role route to student list.

Expected frontend changes:
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/auth/application/role-routing.ts`
- Route tests in `frontend/src/app/app.routes.spec.ts`

Non-break guarantee:
- Existing `/student` links continue to work.

### Iteration 13 - Student Lecture List Page (Join + Re-entry)

Goal: complete student entry point.

Scope:
- Build list page with:
  - join-by-code form and feedback,
  - list of joined lectures,
  - navigation into lecture room route.

Expected frontend changes:
- New student list page component.
- Extend `frontend/src/app/student/application/student-workspace.service.ts` and `frontend/src/app/lecture.service.ts` with list methods.
- Tests for join outcomes and re-entry navigation.

Non-break guarantee:
- Existing answer loop logic still available while room route is introduced.

### Iteration 14 - Student Lecture Room Page

Goal: route-driven answer loop per lecture.

Scope:
- Build `/student/lectures/:lectureId` room page.
- Load next pending question, submit answer, handle cooldown (`429`) and waiting/no-question states.
- Preserve deterministic progression policy from backend.

Expected frontend changes:
- Extract/port answer flow logic currently in `frontend/src/app/student/student-home.ts`.
- Add room component tests for lifecycle and error states.

Non-break guarantee:
- Existing backend progression/submission contracts unchanged.

### Iteration 15 - Token/Code Join Contract Hardening (Explicit Invite Errors)

Goal: make deep-link errors explicit and production-usable.

Scope:
- Refine join-by-token/code behavior to distinguish invalid vs revoked vs expired invites.
- Preserve idempotent success for already enrolled students.

Expected backend changes:
- `JoinLectureUseCase` branch refinement.
- Invite repository query support for explicit state inspection.
- Error code mapping in API responses and integration tests.

Non-break guarantee:
- Happy path for valid token/code unchanged.

### Iteration 16 - Token Deep-Link Completion and Cleanup

Goal: finish phase with complete student journey and remove transitional wiring.

Scope:
- Update token join page to redirect directly to `/student/lectures/:lectureId`.
- Map explicit invite errors to actionable UI messages.
- Remove obsolete query-param hydration path once route-driven room flow is stable.

Expected frontend changes:
- `frontend/src/app/student/student-join-token.ts`
- Student route/page tests including invalid/revoked/expired cases.

Documentation and closure:
- Update `README.md` and `ROADMAP.md` checkboxes as contracts/flows become complete.

Non-break guarantee:
- Backward-compatible redirects remain until all callers use new routes.

## Definition of Done per Iteration

- Single behavior slice completed end-to-end.
- Automated tests updated for changed contracts and key edge cases.
- No regression in previously working instructor/student flows.
- DDD and clean architecture boundaries preserved.
- Any temporary compatibility layer documented and tracked for later removal.

## Suggested Validation Cadence

- Backend slices affecting APIs/security: run targeted ITs, then `./mvnw verify` before merge.
- Frontend slices affecting routes/components: run `bun run test` (non-watch) before merge.
- Cross-layer slices: validate both backend and frontend suites.
