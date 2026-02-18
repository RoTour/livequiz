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
