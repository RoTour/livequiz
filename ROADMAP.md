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

Related plan:
- `plans/1-Student-and-Instructor-workflow.md`

Execution policy:
- Deliver through small, non-breaking iterations defined in the related plan.
- Keep instructor workflow delivery first, then student workflow.
- Preserve DDD + clean architecture boundaries across all slices.

Acceptance criteria for this phase:
- [ ] Instructor can manage more than one lecture end-to-end from UI.
- [ ] Instructor can open any lecture and operate question unlock flow without losing context on refresh.
- [ ] Instructor can see class response progress and per-question student answer history.
- [ ] Student can complete full join and answer journey through UI with no manual API intervention.
- [ ] Token deep-link path is production-usable (not placeholder).
- [ ] Student can re-enter joined lectures and continue progression reliably.

### Iteration checklist
- [x] Iteration 01 - Baseline contract lock (no feature change)
- [x] Iteration 02 - Lecture ownership metadata write path
- [x] Iteration 03 - Instructor lecture listing API (summary read model)
- [x] Iteration 04 - Instructor route scaffold (backward compatible)
- [x] Iteration 05 - Instructor lecture list page
- [x] Iteration 06 - Instructor detail route param source of truth
- [x] Iteration 07 - Instructor ownership enforcement on existing endpoints
- [x] Iteration 08 - Instructor per-question analytics rollup API
- [x] Iteration 09 - Instructor per-question student answer history API
- [x] Iteration 10 - Instructor analytics UI integration
- [ ] Iteration 11 - Student lecture listing API
- [ ] Iteration 12 - Student route scaffold (backward compatible)
- [ ] Iteration 13 - Student lecture list page (join + re-entry)
- [ ] Iteration 14 - Student lecture room page
- [ ] Iteration 15 - Token/code join contract hardening (explicit invite errors)
- [ ] Iteration 16 - Token deep-link completion and cleanup

### Cross-cutting delivery checklist for this phase
- [ ] Keep terminology lecture-centric across DTOs, components, and docs.
- [ ] Keep role authorization explicit in backend security config and frontend guards.
- [ ] Update `README.md` and this roadmap when contracts or journey behavior materially change.
- [ ] Avoid introducing breaking changes to already completed Phase 6 steps unless intentionally planned.
