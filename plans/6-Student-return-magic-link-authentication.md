## Context

Students currently rely on short-lived JWTs and anonymous onboarding. After token expiration, there is no student return path that restores the same `studentId`, enrollments, and progression weeks later.

Existing backend already has:
- student identity lifecycle (`ANONYMOUS`, `REGISTERED_UNVERIFIED`, `REGISTERED_VERIFIED`),
- school email policy enforcement,
- one-time email verification challenges,
- verify endpoint that returns a student JWT.

Business precision confirmed for this feature:
- return authentication uses passwordless email magic link,
- verified students can return with the same identity,
- if a registered but unverified student tries to connect, the received link must both verify and authenticate.

## Goal

Add a production-safe student return authentication flow so a student can request a magic link by school email and re-enter their account with stable identity continuity. The same link flow must also satisfy first verification for registered unverified students and sign them in immediately.

## Scope and Non-goals

In scope:
- New public student endpoint to request magic login link.
- Reuse verification challenge flow so token consumption results in authenticated student JWT.
- Frontend login UX for student magic-link request.
- Error handling, anti-enumeration behavior, and test coverage updates.

Non-goals:
- Student passwords.
- Social login / external identity providers.
- Instructor authentication redesign.
- Changing lecture/invite/submission domain behavior.

## Design principles

- Preserve DDD and Clean Architecture dependency direction: Domain <- Application <- Infrastructure/Interface.
- Keep student identity as the invariant anchor (`studentId` unchanged across anonymous/verified/return flows).
- Keep authentication responses non-enumerating for unknown/unowned emails.
- Reuse existing verification challenge semantics to keep flow small, reversible, and deployable.
- Maintain explicit role boundaries: only server issues `STUDENT` role claims.

## Iterations

### Iteration 01 - Public student magic-link request API
- Type: backend
- Why: introduce return-auth entrypoint without changing verification consumption yet.
- DDD boundary impact: application + infrastructure + interface
- Domain:
  - Reuse `StudentIdentity` ownership and `EmailVerificationChallenge` invariants.
- Application:
  - Add `RequestStudentMagicLoginUseCase`.
  - Normalize/validate email via `StudentEmailPolicy`.
  - Resolve student by email; if found, issue challenge using existing challenge service.
  - Return generic status for both found/not found paths.
- Infrastructure:
  - Wire use case in auth controller.
  - Ensure endpoint is `permitAll` in `SecurityConfig`.
- Interface (API/UI):
  - Add `POST /api/auth/students/request-login` with `{ email }` request and generic status response.
- Risks/open questions:
  - Cooldown and hourly limits may throttle repeated login requests; acceptable and intentionally reused.
- Acceptance criteria:
  - Valid school email returns generic success regardless of identity existence.
  - Invalid format/domain returns existing policy errors.
  - Endpoint is callable without JWT.

### Iteration 02 - Link consumption semantics for verified and unverified students
- Type: backend
- Why: guarantee business rule that link both verifies and signs in when needed.
- DDD boundary impact: domain + application
- Domain:
  - Keep one-time challenge consumption invariant and stable identity invariant.
- Application:
  - Keep/adjust verify-email flow so consumed link always returns authenticated student JWT.
  - Ensure unverified identity transitions to verified on link consumption.
  - Ensure already verified identity can still consume a fresh link and authenticate safely.
- Infrastructure:
  - Extend integration tests for request-login and verify-link outcomes.
- Interface (API/UI):
  - Existing `POST /api/auth/students/verify-email` remains token-consumption endpoint.
- Risks/open questions:
  - Verify endpoint name remains `verify-email`; behavior now also acts as sign-in completion.
- Acceptance criteria:
  - Registered unverified student can request login link; consuming link verifies and authenticates.
  - Registered verified student can request login link; consuming link authenticates.
  - JWT subject remains original `studentId`.

### Iteration 03 - Frontend student return-login UX
- Type: frontend
- Why: expose return path in product UI while preserving instructor login behavior.
- DDD boundary impact: interface + application (frontend)
- Domain:
  - No backend domain change.
- Application:
  - Extend `AuthService` with `requestStudentMagicLogin` API call.
  - Update interceptor skip list for the new public auth endpoint.
- Infrastructure:
  - No backend infra changes.
- Interface (API/UI):
  - Add student section on login page to request magic link by school email.
  - Keep generic success copy and actionable validation error copy.
  - Preserve existing instructor email/password flow.
- Risks/open questions:
  - Prevent UX confusion between instructor password login and student magic-link request via clear section labels.
- Acceptance criteria:
  - Student can request a login link from `/auth/login` without existing session.
  - Frontend does not attach Authorization header to `/api/auth/students/request-login`.
  - Existing instructor login path remains functional.

### Iteration 04 - Contract hardening and documentation
- Type: fullstack
- Why: make new behavior auditable, testable, and understandable for rollout.
- DDD boundary impact: interface + infrastructure + application
- Domain:
  - No new domain model.
- Application:
  - Ensure test suite captures non-enumeration and continuity invariants.
- Infrastructure:
  - Update backend integration tests and frontend unit tests.
- Interface (API/UI):
  - Update README/roadmap docs for student return-login contract and flow.
- Risks/open questions:
  - Copy consistency between verification and sign-in terminology must remain clear.
- Acceptance criteria:
  - Backend and frontend tests pass with new scenarios.
  - Docs describe student return magic-link flow and unverified-to-authenticated behavior.
