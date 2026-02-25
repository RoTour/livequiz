# 4 - Frontend Student Email Auth and Verification Integration Plan

## Context

Backend student identity lifecycle is now implemented (anonymous -> registered unverified -> registered verified) with these endpoints:

- `POST /api/auth/anonymous`
- `POST /api/auth/students/register-email`
- `POST /api/auth/students/resend-verification`
- `POST /api/auth/students/verify-email`

Related plans:
- `plans/2-QR-invite-autojoin-and-anonymous-student-onboarding-plan.md`
- `plans/3-backend-student-email-auth-and-verification-plan.md`

Frontend currently has role-aware routing and student deep-link join (`/student/join/:token`) but does not yet integrate:

- anonymous bootstrap from frontend,
- email verification route handling,
- student email registration/resend UX,
- token claim awareness for `anonymous` / `emailVerified`.

## Goals

- Integrate backend student auth endpoints without breaking current instructor/student flows.
- Keep scan-and-go path frictionless for first-time students.
- Support deferred school email registration (`@ynov.com`) as a non-blocking student action.
- Complete email verification token flow in frontend (`/student/verify-email?token=...`).
- Preserve lecture continuity through token updates and claim transitions.

## Scope and Non-Goals

In scope:

- Angular auth service/interceptor updates,
- route/guard changes for public student deep-links,
- student verification screen and token exchange flow,
- student workspace registration/resend verification UI,
- frontend unit/integration-style tests for new auth behavior.

Out of scope:

- backend contract changes,
- instructor identity provider migration,
- visual redesign beyond targeted student onboarding/verification additions,
- passwordless login request/verify pair (optional backend extension).

## Design Principles

- Keep anonymous onboarding non-blocking for learning flow.
- Treat backend as source of truth for role and verification state.
- Keep anti-enumeration-safe UX wording for register/resend responses.
- Do not force credential login for student invite/verification deep links.
- Prefer explicit user feedback for known backend error codes.

## Backend Contract Mapping (Frontend)

### 1) Anonymous bootstrap

- Endpoint: `POST /api/auth/anonymous`
- Use case: no valid student session while entering student deep-link flow.
- Expected response: `{ token }` with claims:
  - `role: STUDENT`
  - `anonymous: true`
  - `emailVerified: false`

### 2) Register email

- Endpoint: `POST /api/auth/students/register-email`
- Request: `{ email }`
- Expected generic response: `{ status: "VERIFICATION_EMAIL_SENT_IF_ALLOWED" }`
- Known validation errors to map:
  - `EMAIL_REQUIRED`
  - `EMAIL_INVALID_FORMAT`
  - `EMAIL_DOMAIN_NOT_ALLOWED`
  - `EMAIL_VERIFICATION_COOLDOWN`
  - `EMAIL_VERIFICATION_RATE_LIMITED`

### 3) Resend verification

- Endpoint: `POST /api/auth/students/resend-verification`
- Request: `{ email? }` (optional)
- Expected generic response: `{ status: "VERIFICATION_EMAIL_SENT_IF_ALLOWED" }`

### 4) Verify email

- Endpoint: `POST /api/auth/students/verify-email`
- Request: `{ token }`
- Expected success: `{ token }` (new JWT with `anonymous=false`, `emailVerified=true`)
- Known failure codes:
  - `EMAIL_VERIFICATION_TOKEN_REQUIRED`
  - `EMAIL_VERIFICATION_TOKEN_INVALID`
  - `EMAIL_VERIFICATION_TOKEN_CONSUMED`
  - `EMAIL_VERIFICATION_TOKEN_EXPIRED`

## Frontend Integration Targets

High-confidence files to update:

- `frontend/src/app/login/auth.service.ts`
- `frontend/src/app/auth.interceptor.ts`
- `frontend/src/app/app.routes.ts`
- `frontend/src/app/login/auth-guard.ts`
- `frontend/src/app/student/student-join-token.ts`
- `frontend/src/app/student/student-join-token.html`
- `frontend/src/app/student/application/student-workspace.service.ts`
- `frontend/src/app/lecture.service.ts`
- `frontend/src/app/student/student-lecture-list.ts`
- `frontend/src/app/student/student-lecture-list.html`
- `frontend/src/app/student/student-lecture-room.ts`
- `frontend/src/app/student/student-lecture-room.html`

Likely new files:

- `frontend/src/app/student/student-verify-email.ts`
- `frontend/src/app/student/student-verify-email.html`
- (optional) `frontend/src/app/student/application/student-auth.service.ts`

## Iteration Plan

### Iteration 01 - Auth Contract Lock in Frontend

Goal: prevent regressions while introducing new student auth behavior.

Scope:

- Add/update tests around current token role parsing and route guard behavior.
- Confirm existing instructor login and student lecture routes remain stable.

Acceptance:

- Existing role routing tests remain green before feature changes.

### Iteration 02 - Auth Service and Claim Model Expansion

Goal: support student lifecycle claims and endpoint calls.

Scope:

- Extend token parsing in `AuthService` to expose `anonymous` and `emailVerified`.
- Add methods for:
  - anonymous session bootstrap,
  - register email,
  - resend verification,
  - verify email token exchange.

Acceptance:

- Frontend can call all backend student auth endpoints with typed responses.

### Iteration 03 - Interceptor and Public Auth Endpoints Safety

Goal: avoid auth header side effects on permit-all endpoints.

Scope:

- Update interceptor skip rules for:
  - `/api/auth/login`
  - `/api/auth/anonymous`
  - `/api/auth/students/verify-email`
- Add interceptor tests for attach/skip matrix.

Acceptance:

- Invalid/stale token does not block anonymous bootstrap or verify-email endpoint.

### Iteration 04 - Route and Guard Integration for Student Deep Links

Goal: make student invite and verification links work pre-auth.

Scope:

- Add public route for `/student/verify-email`.
- Make `/student/join/:token` public.
- Keep `/student/lectures` and room routes protected.

Acceptance:

- Fresh browser can open join/verify deep links without redirect loop to login.

### Iteration 05 - Token Deep-Link Join with Anonymous Bootstrap

Goal: finalize scan-and-go from frontend.

Scope:

- Update `StudentJoinToken` page flow:
  - instructor token -> redirect instructor workspace,
  - no session/non-student session -> bootstrap anonymous,
  - then execute join by token and route to lecture room.
- Keep explicit invite error states (`INVITE_NOT_FOUND`, `INVITE_REVOKED`, `INVITE_EXPIRED`).

Acceptance:

- Invite QR in fresh browser auto-joins lecture with generated student session.

### Iteration 06 - Student Verify Email Page

Goal: complete verification token exchange and session upgrade.

Scope:

- Add `/student/verify-email` page reading `token` query param.
- Call verify endpoint, store returned JWT, and redirect to `/student/lectures`.
- Render explicit UX for invalid/expired/consumed token states.

Acceptance:

- Valid token upgrades frontend session and redirects to student workspace.

### Iteration 07 - Non-Blocking Registration CTA in Student Workspace

Goal: allow deferred registration while preserving class flow.

Scope:

- Add register/resend UI in:
  - student lecture list,
  - student lecture room.
- Keep success response generic and non-enumerating.
- Show actionable validation errors for invalid format/domain and cooldown/rate limit.

Acceptance:

- Anonymous or unverified student can start verification without leaving lecture flow.

### Iteration 08 - Hardening and Tests

Goal: ensure deterministic behavior on critical auth transitions.

Scope:

- Update/add tests for:
  - auth service claim parsing and endpoint methods,
  - interceptor skip rules,
  - route guards/deep-link behavior,
  - student join token auto-bootstrap path,
  - student verify email success/failure flow.

Acceptance:

- Frontend tests cover happy paths and key failure modes for student auth lifecycle.

## Test File Plan

Update existing:

- `frontend/src/app/app.routes.spec.ts`
- `frontend/src/app/login/auth.service.spec.ts`
- `frontend/src/app/login/auth-guard.spec.ts`
- `frontend/src/app/student/student-join-token.spec.ts`
- `frontend/src/app/student/student-lecture-list.spec.ts`
- `frontend/src/app/student/student-lecture-room.spec.ts`
- `frontend/src/app/login/login.spec.ts`

Add new:

- `frontend/src/app/auth.interceptor.spec.ts`
- `frontend/src/app/student/student-verify-email.spec.ts`
- (if split service) `frontend/src/app/student/application/student-auth.service.spec.ts`

## Risks and Mitigations

- Token state split risk (`AuthService` signal vs localStorage reads):
  - Mitigation: centralize token updates through `AuthService`, keep interceptor stateless and predictable.

- Deep-link auth loops risk:
  - Mitigation: keep join/verify routes public, bootstrap anonymous only when needed.

- Anti-enumeration leakage via UX copy:
  - Mitigation: use generic success wording for register/resend regardless of ownership outcome.

## Suggested Rollout Order

1. Iteration 01-03 (auth foundation and safe transport)
2. Iteration 04-06 (deep-link join + verify page)
3. Iteration 07 (workspace CTA)
4. Iteration 08 (tests and hardening)

## Definition of Done

- Student deep-link join works in fresh browser without prior login.
- Frontend supports anonymous student sessions via backend endpoint.
- Student can register and resend school-email verification from workspace screens.
- Verification link route upgrades token and preserves student route continuity.
- Known backend error codes are surfaced with clear and safe UX messaging.
- Frontend test suite passes for updated auth/route/student flows.
