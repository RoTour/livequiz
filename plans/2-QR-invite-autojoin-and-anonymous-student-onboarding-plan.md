# 2 - QR Invite Autojoin and Anonymous Student Onboarding Plan

## Context

The platform already supports invite creation with token/code and student join endpoints.

What is missing for scan-and-go UX:
- instructor UI does not render a QR image or copy-link action
- `/student/join/:token` is not yet auto-joining students
- anonymous student identity does not exist yet

## Goals

- Students can scan a QR code and join with minimal friction.
- If no session exists, an anonymous student session is created automatically.
- Anonymous students can answer immediately.
- Later, students can register school email and verify it without losing progress.

## Phase A - QR + Auto-Join + Anonymous Bootstrap

### A1. Instructor invite UX

- Render QR image from invite `joinUrl`.
- Add copy-link button with success feedback.
- Keep URL visible as fallback.

Acceptance:
- Instructor can scan QR from another device and open join flow.
- Copy button copies exact invite link.

### A2. Public token landing route

- Make `/student/join/:token` accessible before authenticated student guard.
- Route behavior:
  - unauthenticated: bootstrap student session then join
  - student authenticated: join directly
  - instructor authenticated: redirect to instructor workspace

Acceptance:
- Scanning invite QR in a fresh browser can reach auto-join flow.

### A3. Anonymous auth issuance (backend)

- Add `POST /api/auth/anonymous` endpoint.
- Return JWT with `role=STUDENT`, `anonymous=true`, and generated `sub`.
- Permit endpoint in security configuration.
- Reuse existing JWT filter/authorization.

Acceptance:
- Frontend can request anonymous token and call student endpoints.

### A4. Auto-join flow wiring (frontend)

- Add auth service method to ensure student session:
  - keep existing student token when valid
  - otherwise call `/api/auth/anonymous`
- Student token page uses token route param and calls join API automatically.
- Redirect to `/student` with joined lecture selected.

Acceptance:
- Scanned token results in automatic enrollment and student landing page.

### A5. Behavior tests

- Backend integration test: anonymous token can join and submit.
- Frontend tests:
  - student token route auto-joins
  - invalid/revoked/expired token shows actionable error
  - QR render + copy-link interaction

## Phase B - Deferred School Email Registration (follow-up)

### B1. Identity model

- Add student lifecycle states:
  - `ANONYMOUS`
  - `REGISTERED_UNVERIFIED`
  - `REGISTERED_VERIFIED`
- Keep stable student id so existing enrollment/submissions remain linked.

### B2. Registration start

- Add endpoint to attach school email to current student identity.
- Trigger verification email.
- Validate allowed domain policy.

### B3. Verification completion

- Add endpoint to validate verification token/OTP.
- Mark student identity as verified.
- Optionally refresh token claim (`anonymous=false`).

### B4. UX

- Show non-blocking CTA in student workspace: register email later.
- Preserve uninterrupted answer flow for anonymous users.

### B5. Behavior tests

- Anonymous user can answer before registration.
- Registration later preserves prior progress.
- Invalid/expired verification paths are explicit.

## Rollout order

1. QR render + copy UX
2. Public token join route behavior
3. Anonymous auth endpoint + security
4. Auto-join integration
5. Behavior tests and hardening
6. Deferred registration feature set
