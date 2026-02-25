# 3 - Backend Student Email Authentication and Verification Plan

## Context

Current backend authentication is still based on static in-memory users (`/api/auth/login`), while student identity in lecture flows is effectively the JWT subject string.

Related plan:
- `plans/2-QR-invite-autojoin-and-anonymous-student-onboarding-plan.md`

Plan 2 already establishes:
- anonymous student bootstrap (`POST /api/auth/anonymous`) for scan-and-go,
- deferred student registration and verification as a follow-up.

This plan defines the backend implementation for that deferred identity flow, with strict school-domain policy.

## Goals

- Add backend student authentication based on school email.
- Allow only emails under `@ynov.com`.
- Default student role to `STUDENT` (no role choice from client).
- Add a complete email verification system before treating email identity as verified.
- Preserve existing lecture enrollment/submission continuity when anonymous students verify later.

## Scope and Non-Goals

In scope:
- backend domain/application/infrastructure changes,
- persistence migrations,
- API contracts and security mapping,
- automated tests for auth + verification behavior.

Out of scope (for this slice):
- frontend UX implementation details,
- instructor identity provider migration,
- changing lecture domain behavior unrelated to identity.

## Design Principles

- Keep DDD + clean architecture boundaries.
- Keep student identity stable across lifecycle states.
- Do not trust client-provided role or student id; derive from verified backend state/JWT.
- Return explicit API error codes for domain-policy failures.
- Avoid account/email enumeration through response design and throttling.

## Target Identity Model

### Student lifecycle states

- `ANONYMOUS`
- `REGISTERED_UNVERIFIED`
- `REGISTERED_VERIFIED`

### Core entities (backend)

- `StudentIdentity`
  - `studentId` (stable technical id, used by lecture enrollment/submission)
  - `email` (nullable for anonymous)
  - `status`
  - `emailVerifiedAt` (nullable)
  - `createdAt`, `updatedAt`

- `EmailVerificationChallenge`
  - `challengeId`
  - `studentId`
  - `email`
  - `tokenHash` (never store raw token)
  - `expiresAt`
  - `consumedAt` (nullable)
  - `createdAt`

## API Contract Plan (Backend)

### Keep from Plan 2

- `POST /api/auth/anonymous`
  - issues JWT with `role=STUDENT`, `anonymous=true`.

### Add for email registration and validation

- `POST /api/auth/students/register-email`
  - input: `email`
  - behavior:
    - require authenticated student identity (anonymous or student)
    - normalize + validate domain policy (`@ynov.com` only)
    - set status to `REGISTERED_UNVERIFIED`
    - create verification challenge and send email
  - response: generic success payload (non-enumerating)

- `POST /api/auth/students/verify-email`
  - input: verification token (or code)
  - behavior:
    - validate token exists, unexpired, unused
    - mark challenge consumed
    - mark student status `REGISTERED_VERIFIED`
    - issue refreshed JWT with `anonymous=false`, `role=STUDENT`
  - response: `{ token }`

- `POST /api/auth/students/resend-verification`
  - input: optional `email` (or infer from authenticated student)
  - behavior: issue new challenge with resend throttling
  - response: generic success payload

Optional (if returning verified users must sign in without existing token):
- `POST /api/auth/students/request-login`
- `POST /api/auth/students/verify-login`

This optional pair can reuse the same challenge infrastructure for passwordless email login.

## Domain Policy Rules

- Email normalization:
  - trim spaces,
  - lowercase local/domain parts for comparison/storage,
  - reject malformed addresses.

- Allowed domain:
  - exact domain match `ynov.com`.
  - reject subdomains unless explicitly approved later.

- Role rules:
  - role always server-assigned,
  - default/forced student role for student auth endpoints.

- Verification rules:
  - one-time use token,
  - strict TTL,
  - resend cooldown + max attempts.

## Iteration Plan

### Iteration 01 - Baseline Auth Contract Lock

Goal: protect existing behavior before identity refactor.

Scope:
- Add/extend integration tests for:
  - anonymous auth endpoint behavior (from plan 2),
  - student JWT role defaults,
  - protected route access with student identity.

Acceptance:
- Existing student/instructor workflows remain green.

### Iteration 02 - Persistence and Migration for Student Identity

Goal: persist student identity + verification challenges.

Scope:
- Add Flyway migration creating:
  - `student_identities`,
  - `email_verification_challenges`,
  - indexes for lookup by `student_id`, `email`, `token_hash`, expiration.
- Add domain repositories + JPA/in-memory adapters.

Acceptance:
- Identity/challenge lifecycle persists in Postgres and works in in-memory profile.

### Iteration 03 - Email Policy and Validation Primitives

Goal: centralize `@ynov.com` policy and input validation.

Scope:
- Add domain/application validator/value object for school email.
- Add explicit API errors:
  - `EMAIL_REQUIRED`
  - `EMAIL_INVALID_FORMAT`
  - `EMAIL_DOMAIN_NOT_ALLOWED`

Acceptance:
- Any non-`@ynov.com` email is rejected consistently across all auth endpoints.

### Iteration 04 - Register Email Use Case

Goal: let current student identity start registration.

Scope:
- Implement application use case:
  - resolve current student identity,
  - attach/replace email in unverified state,
  - create verification challenge,
  - trigger email sender adapter.
- Ensure stable `studentId` is preserved for anonymous upgrades.

Acceptance:
- Anonymous student can register email later without losing enrollment/submissions.

### Iteration 05 - Verify Email Use Case and JWT Refresh

Goal: finalize identity verification.

Scope:
- Implement token validation + challenge consumption.
- Transition identity status to `REGISTERED_VERIFIED`.
- Issue refreshed student JWT with verification-related claims.

Acceptance:
- Valid token verifies exactly once and returns a student JWT.
- Expired/consumed token returns explicit error.

### Iteration 06 - Security Configuration and Endpoint Wiring

Goal: expose new auth endpoints safely.

Scope:
- Add new endpoints in auth controller(s).
- Update `SecurityConfig` permit rules for registration/verification endpoints.
- Keep instructor-only/student-only route protection unchanged.

Acceptance:
- New auth endpoints are reachable as intended.
- Existing lecture endpoint role guards remain intact.

### Iteration 07 - Email Delivery Infrastructure

Goal: provide production-ready email validation delivery with local fallback.

Scope:
- Add mail adapter interface in application layer.
- Implement infrastructure adapter (SMTP/provider-backed).
- Add non-prod fallback adapter (logs challenge token for local/test).
- Add configuration properties:
  - allowed domain (default `ynov.com`),
  - verification TTL,
  - resend cooldown,
  - frontend verification URL base.

Acceptance:
- Verification emails are emitted in runtime profiles.
- Local/in-memory flows remain testable without external provider.

### Iteration 08 - Hardening, Threat Controls, and Tests

Goal: make auth flow safe and diagnosable.

Scope:
- Add rate limits/throttles for register/resend/verify attempts.
- Add anti-enumeration response behavior.
- Add audit logs for auth lifecycle events.
- Add integration tests for:
  - allowed domain acceptance,
  - forbidden domain rejection,
  - verification success/failure paths,
  - idempotent/duplicate verification handling.

Acceptance:
- Security-sensitive edge cases are covered and deterministic.

## Mapping to Plan 2

- Plan 2 Phase A (`anonymous bootstrap`) remains the frictionless entry point.
- This plan operationalizes Plan 2 Phase B backend details:
  - B1 identity states,
  - B2 registration start + domain policy,
  - B3 verification completion + claim refresh,
  - B5 behavior tests.

## Suggested Rollout Order

1. Iteration 01-03 (foundations + policy)
2. Iteration 04-05 (register + verify use cases)
3. Iteration 06-07 (controller/security + email delivery)
4. Iteration 08 (hardening and security tests)

## Definition of Done

- Student auth flow supports anonymous -> registered -> verified lifecycle.
- Only `@ynov.com` emails can be registered/verified.
- Issued student JWT always carries role `STUDENT` by default.
- Verification is one-time, expiring, and auditable.
- Integration tests pass for both happy path and key failure modes.
