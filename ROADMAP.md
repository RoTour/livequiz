# Roadmap

We build LiveQuiz incrementally, always delivering a fully connected vertical slice.

## Phase 1: Walking Skeleton
Goal: backend + frontend connectivity with authentication and health visibility.

- [x] Backend bootstrapped and running (`Spring Boot`)
- [x] Frontend bootstrapped and running (`Angular`)
- [x] Health check endpoint and frontend status widget
- [x] JWT login flow and protected routes

## Phase 2: Lecture Foundation
Goal: instructor can create and manage a lecture aggregate.

- [x] Lecture aggregate created (`Lecture`, `LectureId`)
- [x] `CreateLecture` use case and API (`POST /api/lectures`)
- [x] Add ordered questions to lecture
- [x] Unlock question behavior (`unlock specific`, `unlock next`)
- [x] Read lecture state endpoint for UI sync

## Phase 3: Invite-based Enrollment
Goal: instructor controls enrollment with reusable class invite.

- [x] Instructor can generate lecture invite
- [x] Invite contains short code (6 chars) + token URL for QR
- [x] Invite expiration capped at 24h
- [x] Student join endpoint is idempotent (`200` for already enrolled)
- [x] Enrollment required before student can access question flow

## Phase 4: Student Progression and Submission Policy
Goal: students progress deterministically through unlocked questions.

- [x] Next question policy: oldest unlocked and not yet submitted
- [x] Submission endpoint stores history as append-only attempts
- [x] Latest attempt treated as canonical
- [x] Resubmission cooldown with `429` and retry metadata

## Phase 5: Hardening and Scale
Goal: production-ready reliability, security, and observability.

- [ ] Persist all lecture/invite/enrollment/submission models in Postgres profile
- [ ] Replace default credentials with managed identity provider
- [ ] Add migration tooling (`Flyway` or `Liquibase`)
- [ ] Add role-aware instructor/student dashboards
- [ ] Add structured audit logs and rate limiting for join/submission flows

## Phase 6: Role-aware Frontend UX Rollout
Goal: ship an appealing, role-first UI in reviewable steps.

Related plan:
- `plans/2-QR-invite-autojoin-and-anonymous-student-onboarding-plan.md`

- [x] Step 1: role-aware auth + route foundations (`/instructor`, `/student`, role guards, login redirect)
- [x] Step 2: shared app shell + role-based navigation + logout flow
- [x] Step 3: instructor workspace migration (create lecture, add question, unlock, state)
- [x] Step 4: complete invite management (list/revoke with clear active/revoked states)
- [x] Step 5: student workspace migration (join by code, next question, submit + cooldown UX)
- [x] Step 6: invite token deep-link (`/student/join/:token`) with auto-join flow
- [x] Step 7: cohesive visual system (typography, color tokens, spacing, responsive polish)
- [x] Step 8: behavior-focused frontend tests across role flows and critical components

## Phase 7: Complete End-to-End Role Workflows (Part 2)
Goal: move from feature fragments to a complete, lecture-driven classroom product flow, delivered in two explicit steps: instructor workflow first, then student workflow.

### Why this phase exists
- Current implementation has core mechanics (create lecture, question unlock, invite, join, submit) but not a complete route-driven product journey.
- Instructor workspace currently assumes one active lecture in component state and lacks multi-lecture selection/lifecycle.
- Student workspace currently lacks a complete lecture-room model and token deep-link flow completion.
- Submission data exists, but instructor-facing analytics/history UX for classroom monitoring is not yet delivered.

### Non-goals for this phase
- No identity-provider migration yet (keep current JWT auth model).
- No major domain rename/refactor away from lecture-centric language.
- No broad visual redesign that breaks existing role-aware shell behavior.

### Step 1: Instructor Workflow (first, required before Step 2)
Goal: make instructor usage complete from first login to live class monitoring.

#### Product journey to support
- Instructor logs in and lands on a lecture list/workspace entry.
- Instructor can create multiple lectures and return to any prior lecture.
- Instructor can open a specific lecture, add ordered questions, unlock next/specific questions, and monitor class progress.
- Instructor can review per-question answer history to detect struggling/not-yet-responding students.

#### Backend work (instructor)
- [ ] Add lecture listing capability scoped to current instructor.
  - Extend lecture model persistence with ownership metadata (`createdByInstructorId`, `createdAt`).
  - Add repository read path for "lectures owned by instructor".
  - Keep existing lecture aggregate invariants intact.
- [ ] Add instructor lecture summary read model.
  - Response shape should include lecture identity, title, creation timestamp, question count, unlocked count.
  - Designed for fast rendering in lecture list cards/table.
- [ ] Add instructor analytics endpoints for lecture detail.
  - Per-question rollup: enrolled, answered, unanswered, multi-attempt count.
  - Per-question student answer history: studentId, latest answer timestamp, attempt count, latest answer text (or preview).
  - Ensure these endpoints are instructor-only in `SecurityConfig`.
- [ ] Keep DDD boundaries clear.
  - Domain enforces invariants.
  - Application orchestrates queries and policies.
  - Infrastructure maps API DTOs and persistence models.

#### Frontend work (instructor)
- [ ] Add/activate route structure for instructor workflow.
  - `/instructor/lectures` => lecture list + create lecture entry point.
  - `/instructor/lectures/:lectureId` => lecture detail workspace.
- [ ] Build instructor lecture list page.
  - Empty state for first login (no lectures yet).
  - Create lecture action with optimistic or immediate refresh behavior.
  - Clear selection/navigation into lecture detail.
- [ ] Refactor instructor detail page from local selected-lecture signal to route-param source of truth.
  - Direct URL refresh should preserve selected lecture context.
  - Existing create/add/unlock/invite behavior must continue working.
- [ ] Add question state and analytics panels in lecture detail.
  - Show each question status (`locked`/`unlocked`) plus answer progress stats.
  - Add drilldown action to view student answer history per question.
- [ ] Keep invite management integrated in lecture detail.
  - Generate, list, revoke invites as currently implemented.
  - Maintain clear state indicators: active/revoked/expired.

#### UX and design requirements (instructor)
- [ ] Preserve current shared app shell and role navigation.
- [ ] Use consistent status tokens/chips for lecture/question states.
- [ ] Prioritize decision speed for instructor (scanable metrics, minimal click depth).
- [ ] Ensure responsive behavior on laptop/tablet where instructors commonly operate.

#### Testing and validation (instructor)
- [ ] Backend integration tests for:
  - lecture listing by instructor ownership,
  - analytics and history endpoint authorization + response contracts.
- [ ] Frontend tests for:
  - lecture list/create/select navigation,
  - route-param-driven lecture detail behavior,
  - analytics/history rendering and empty/error states.
- [ ] Run relevant suites before merge:
  - backend integration tests (`./mvnw verify` or targeted ITs),
  - frontend tests (`bun run test` or equivalent non-watch command).

#### Step 1 acceptance criteria
- Instructor can manage more than one lecture end-to-end from UI.
- Instructor can open any lecture and operate question unlock flow without losing context on refresh.
- Instructor can see class response progress and per-question student answer history.

### Step 2: Student Workflow (second)
Goal: make student participation complete from join entry to lecture completion states.

#### Product journey to support
- Student joins via invite code or token deep-link.
- Student enters a lecture room context and repeatedly answers oldest unlocked pending questions.
- Student gets clear waiting/empty/cooldown feedback and can re-enter lecture sessions later.

#### Backend work (student)
- [ ] Add/confirm student lecture listing read model.
  - List lectures the current student is enrolled in.
  - Include minimal metadata needed by student lecture list UI.
- [ ] Complete token join support contract for deep-link route.
  - Ensure token join behavior is robust and consistent with code-based join.
  - Return explicit business errors for invalid/revoked/expired invite states.
- [ ] Keep progression policy unchanged unless explicitly revised.
  - Oldest unlocked unanswered question remains the deterministic next question.

#### Frontend work (student)
- [ ] Add/activate route structure for student workflow.
  - `/student/lectures` => joined lecture list + join by code.
  - `/student/lectures/:lectureId` => lecture room (question/submit flow).
  - `/student/join/:token` => auto-join then redirect into lecture room.
- [ ] Build student lecture list page.
  - Join-by-code input + feedback.
  - List joined lectures for re-entry.
- [ ] Build student lecture room page.
  - Load next pending question.
  - Submit answer.
  - Display cooldown (`429`) with retry hint.
  - Display "no unlocked pending question" and waiting states clearly.
- [ ] Complete token deep-link component behavior.
  - Resolve token from route.
  - Execute join automatically.
  - Handle invalid/revoked/expired states with actionable messages.
  - Redirect success path to lecture room route.

#### UX and design requirements (student)
- [ ] Mobile-first readability and tap targets for classroom phone usage.
- [ ] Minimal-friction answer loop (question visibility, answer box, submit state, next transition).
- [ ] Clear distinction between hard errors and normal waiting/no-question states.

#### Testing and validation (student)
- [ ] Backend integration tests for token/code join variants and enrollment constraints.
- [ ] Frontend tests for token auto-join route, lecture room lifecycle, and cooldown UX.
- [ ] Maintain existing behavior coverage for `next-question` and submission flows.

#### Step 2 acceptance criteria
- Student can complete full join and answer journey through UI with no manual API intervention.
- Token deep-link path is production-usable (not placeholder).
- Student can re-enter joined lectures and continue progression reliably.

### Cross-cutting delivery checklist for this phase
- [ ] Keep terminology lecture-centric across DTOs, components, and docs.
- [ ] Keep role authorization explicit in backend security config and frontend guards.
- [ ] Update `README.md` and this roadmap when contracts or journey behavior materially change.
- [ ] Avoid introducing breaking changes to already completed Phase 6 steps unless intentionally planned.
