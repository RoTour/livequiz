# 1 - Teacher Identity and Role Classification Plan

## Context

Student authentication now supports anonymous and email-verified lifecycle states, while instructor login still derives JWT role from authentication authorities at login time.

Current behavior does not provide a dedicated business source of truth for "actual teachers". We need deterministic role classification so users are recognized as `INSTRUCTOR` only when explicitly registered as teachers in backend data.

Related context:
- `plans/3-backend-student-email-auth-and-verification-plan.md`
- `plans/4-frontend-student-email-auth-and-verification-integration-plan.md`
- `backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/JwtController.java`
- `backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/JwtService.java`
- `backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java`

## Goal

Establish a backend-owned teacher classification model that issues `INSTRUCTOR` JWT role only for users present in a teacher registry, with all other authenticated users issued `STUDENT`.

Success criteria:
- Instructor-only endpoints are inaccessible to users not classified as teacher.
- JWT role claim is derived from backend teacher registry, not client input.
- Existing student flows (anonymous/register/verify/join/submit) remain unchanged.

## Scope and Non-goals

In scope:
- Introduce teacher identity registry in backend persistence.
- Add domain/application services to classify authenticated users into `INSTRUCTOR` or `STUDENT`.
- Update JWT issuance path to use classification service.
- Keep frontend behavior aligned with token role semantics and add focused tests.
- Add migration and seed support for teacher entries.

Non-goals:
- External IdP/group integration.
- Full admin UI for teacher management.
- Multi-role session switching in one JWT.
- Immediate token revocation infrastructure beyond existing expiration model.

## Design principles

- Backend is the source of truth for role assignment.
- Default to least privilege: unknown authenticated user is `STUDENT`.
- Preserve DDD and Clean Architecture boundaries:
  - Domain defines teacher classification rules and invariants.
  - Application orchestrates role resolution for login/auth use cases.
  - Infrastructure persists teacher records and adapts auth providers.
  - Interface layer remains thin and transport-focused.
- Keep rollout reversible and low risk through incremental, test-backed slices.
- Preserve existing lecture-centric domain behaviors and student lifecycle contracts.

## Iterations

- [Iteration 01 - Auth Role Baseline Lock]
  - Type: backend
  - Why: Freeze current auth behavior with regression tests before changing role issuance logic.
  - Domain: No domain model changes.
  - Application: No application model changes.
  - Infrastructure: Add/adjust tests around `JwtController` login role issuance and `SecurityConfig` protections.
  - Interface: No endpoint contract change.
  - Risks/open questions: Existing tests may rely on authority-derived instructor role assumptions.
  - Acceptance criteria:
    - Tests explicitly capture current login role issuance and protected route access patterns.
    - Baseline tests pass before moving to classification changes.

- [Iteration 02 - Teacher Identity Domain Boundary]
  - Type: backend
  - Why: Introduce explicit domain concept for teacher identity to avoid role logic leaking into controllers.
  - Domain: Add `TeacherIdentity` aggregate/value model and `TeacherIdentityRepository` abstraction with invariant: identity key is unique and normalized.
  - Application: Add `ResolveUserRoleUseCase` or equivalent service contract returning `INSTRUCTOR` when teacher exists and active, else `STUDENT`.
  - Infrastructure: No persistence wiring yet (in-memory stub acceptable for this slice).
  - Interface: No endpoint change yet.
  - Risks/open questions: Identity key normalization rule must match login principal format (username/email).
  - Acceptance criteria:
    - Domain and application layer compile with no framework dependencies crossing inward.
    - Unit tests verify role resolution defaults to `STUDENT` when teacher record is absent.

- [Iteration 03 - Teacher Registry Persistence Adapters]
  - Type: backend
  - Why: Provide concrete source of truth for teacher classification in both `postgres` and `in-memory` profiles.
  - Domain: No rule changes beyond repository contract usage.
  - Application: No orchestration change beyond repository dependency.
  - Infrastructure: Add Flyway migration for `teacher_identities` table (unique normalized identifier, active flag, timestamps), JPA entity/repository/adapter for `postgres`, and in-memory adapter for in-memory profiles; add optional seed records for local/dev.
  - Interface: No API contract change.
  - Risks/open questions: Seed strategy must not create production-default teachers unintentionally.
  - Acceptance criteria:
    - Teacher records can be read via repository in both runtime profiles.
    - Migration applies successfully and is backward compatible.
    - Seed behavior is environment-safe (dev/test only or explicit opt-in).

- [Iteration 04 - JWT Issuance via Teacher Classification]
  - Type: backend
  - Why: Enforce backend-determined role claim at token issuance point.
  - Domain: No new domain objects.
  - Application: Wire `ResolveUserRoleUseCase` into login flow orchestration.
  - Infrastructure: Update login path (`JwtController` and/or dedicated auth application service) to compute role from teacher registry after credential authentication; keep student-auth endpoints issuing `STUDENT` tokens unchanged.
  - Interface: `POST /api/auth/login` response shape remains `{ token }`.
  - Risks/open questions: Existing in-memory users with instructor authority but no teacher record will now receive `STUDENT`.
  - Acceptance criteria:
    - Authenticated user in teacher registry receives JWT with `role=INSTRUCTOR`.
    - Authenticated user not in teacher registry receives JWT with `role=STUDENT`.
    - Student anonymous/email verification flows still issue `role=STUDENT` and preserve current claims.

- [Iteration 05 - Frontend Contract Confirmation]
  - Type: frontend
  - Why: Ensure UI/route behavior stays correct with backend-owned role claim.
  - Domain: Frontend domain types unchanged (`UserRole`).
  - Application: Confirm `AuthService` and role-routing logic consume role claim without hardcoded teacher assumptions.
  - Infrastructure: No backend change.
  - Interface: Update frontend tests for login routing and unknown-role fallback where needed.
  - Risks/open questions: Existing test fixtures may assume authority-derived role issuance patterns.
  - Acceptance criteria:
    - Frontend auth and guard tests pass using backend-issued role claim semantics.
    - Instructor UI remains reachable only when token claim role is `INSTRUCTOR`.

- [Iteration 06 - Hardening, Auditability, and Rollout Safety]
  - Type: backend
  - Why: Make classification operationally safe and diagnosable in production rollout.
  - Domain: No new domain concepts.
  - Application: Add structured audit events for role classification decisions at login (without leaking sensitive secrets).
  - Infrastructure: Add config toggle/fallback policy for controlled rollout; ensure logs/metrics show classification source and outcome.
  - Interface: No API response change.
  - Risks/open questions: Logging verbosity must balance audit needs and privacy.
  - Acceptance criteria:
    - Classification decision is auditable in logs with principal identifier and resolved role.
    - Rollout toggle allows safe activation/deactivation without schema rollback.
    - Full backend test suite remains green.
