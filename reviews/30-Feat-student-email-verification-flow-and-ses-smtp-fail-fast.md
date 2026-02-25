[README](backend/README.md)
Documented AWS SES SMTP production configuration for student verification emails.
- Added a dedicated section listing required env vars (`LIVEQUIZ_STUDENT_EMAIL_VERIFICATION_*`, `SPRING_MAIL_*`).
- Clarified that startup now performs fail-fast validation when SMTP sending is enabled.

[EmailSmtpConfigurationValidator](backend/src/main/java/com/livequiz/backend/infrastructure/email/EmailSmtpConfigurationValidator.java)
Added a fail-fast infrastructure component for SMTP configuration safety.
- New Spring `@Component` class `EmailSmtpConfigurationValidator` validates mail settings during bean construction.
- Non-trivial constructor logic checks `smtpEnabled` and aggregates invalid properties before throwing one `IllegalStateException`.
- Non-trivial helper methods `requireNonBlank`, `requirePositivePort`, and `requireTrue` enforce required host/port/credentials/TLS flags.
- Enforces non-default sender requirement by rejecting `no-reply@livequiz.local` when SMTP is enabled.

[EmailSmtpConfigurationValidatorTest](backend/src/test/java/com/livequiz/backend/infrastructure/email/EmailSmtpConfigurationValidatorTest.java)
Added focused unit tests for the new SMTP fail-fast validator.
- Non-trivial test `shouldFailFastWhenSmtpIsEnabledButRequiredSettingsAreMissing` asserts a startup-blocking `IllegalStateException` with specific missing-property messages.
- `shouldAllowMissingSmtpSettingsWhenSmtpIsDisabled` verifies feature flag behavior.
- `shouldAllowSmtpWhenAllRequiredSettingsAreValid` uses `MockEnvironment` to validate the happy-path SES-like config.

[app.routes.spec](frontend/src/app/app.routes.spec.ts)
Updated routing contract tests for public student deep-link flows.
- Added `StudentVerifyEmail` route assertions.
- Non-trivial route policy change validated: `/student/join/:token` and `/student/verify-email` are now expected to be public (no `canActivate`).

[app.routes](frontend/src/app/app.routes.ts)
Extended route table for student verification and deep-link access.
- Added `StudentVerifyEmail` component route at `/student/verify-email`.
- Removed guards from `/student/join/:token` to support scan-and-go entry without prior auth.

[auth.interceptor](frontend/src/app/auth.interceptor.ts)
Refactored auth-header skip logic into a centralized endpoint list.
- Introduced `SKIPPED_AUTH_ENDPOINTS` constant for `/api/auth/login`, `/api/auth/anonymous`, and `/api/auth/students/verify-email`.
- Non-trivial behavior: interceptor now skips bearer injection for all permit-all auth endpoints used in bootstrap/verification flows.

[auth.interceptor.spec](frontend/src/app/auth.interceptor.spec.ts)
Added explicit interceptor tests for attach/skip matrix.
- Verifies header attachment on protected requests.
- Non-trivial coverage confirms token is not attached for login, anonymous bootstrap, and verify-email endpoints.

[auth-guard.spec](frontend/src/app/login/auth-guard.spec.ts)
Adjusted guard expectations to align with student deep-link policy.
- Test routes now target protected workspace URL `/student/lectures` instead of public join-token route.
- Preserves returnUrl behavior assertions for unauthenticated redirects.

[auth.service.spec](frontend/src/app/login/auth.service.spec.ts)
Expanded AuthService test suite from role-only checks to claim lifecycle and endpoint interactions.
- Refactored token factory to support arbitrary JWT claims (`createToken(claims)`).
- Non-trivial tests added for methods `ensureStudentSession`, `verifyStudentEmail`, `registerStudentEmail`, and `resendStudentVerification`.
- Added state assertions for computed claims (`isAnonymousStudent`, `isStudentEmailVerified`) and invalid-token handling (`isAuthenticated` false).

[auth.service](frontend/src/app/login/auth.service.ts)
Upgraded session management to support student identity lifecycle claims and email verification flows.
- Added computed `claims`, `isAnonymousStudent`, and `isStudentEmailVerified` in class `AuthService`.
- Non-trivial method `ensureStudentSession` bootstraps anonymous student sessions through `/api/auth/anonymous` when needed.
- Added API methods `issueAnonymousStudentToken`, `registerStudentEmail`, `resendStudentVerification`, and `verifyStudentEmail`.
- Introduced non-trivial `setTokenState` centralization to keep signal and `localStorage` updates consistent across login/logout/verification.
- Replaced `extractRole` with broader `extractClaims` decoder and role derivation from decoded claim payload.

[student-join-token.spec](frontend/src/app/student/student-join-token.spec.ts)
Enhanced component tests for session bootstrap and instructor guardrails.
- Added `AuthService` mocking (`ensureStudentSession`, `role`) and shared async initializer helper.
- Non-trivial scenario added: instructor opening a student invite is redirected to `/instructor/lectures`.
- Existing invite error-path tests were updated to use unified initialization flow.

[student-join-token](frontend/src/app/student/student-join-token.ts)
Hardened deep-link join logic for mixed session contexts.
- Injected `AuthService` into `StudentJoinToken`.
- Non-trivial `ngOnInit` flow now handles: missing token, instructor session redirect, anonymous bootstrap via `ensureStudentSession`, then token-based join.
- Added explicit status/error transitions for bootstrap failure and instructor misuse of student links.

[student-lecture-list template](frontend/src/app/student/student-lecture-list.html)
Added non-blocking email verification UI to student workspace list screen.
- New card introduces register/resend actions tied to form and busy state.
- Uses auth claim signals (`isStudentEmailVerified`, `isAnonymousStudent`) for conditional UX messaging.

[student-lecture-list.spec](frontend/src/app/student/student-lecture-list.spec.ts)
Updated test bed dependencies to support added verification UX dependencies.
- Added `ToastService` and `AuthService` stubs with claim signals and verification methods.
- Keeps list/join behavior tests operable after new injected collaborators.

[student-lecture-list](frontend/src/app/student/student-lecture-list.ts)
Integrated student email verification orchestration into lecture list component.
- Injected `AuthService` and added reactive form `registerEmailForm`.
- Non-trivial async methods `registerEmail` and `resendVerification` call backend auth endpoints and synchronize busy/status/error/toast feedback.
- Added non-trivial error-code mapping method `resolveEmailVerificationErrorMessage` for backend codes (`EMAIL_DOMAIN_NOT_ALLOWED`, cooldown/rate-limit, etc.).

[student-lecture-room template](frontend/src/app/student/student-lecture-room.html)
Mirrored verification UX card into lecture room screen.
- Added in-place register/resend email UI to avoid leaving active lecture flow.
- Reuses claim-based conditionals and status/error feedback rendering.

[student-lecture-room.spec](frontend/src/app/student/student-lecture-room.spec.ts)
Extended test configuration for new auth-driven verification controls.
- Added `AuthService` provider mock with signal-backed claim state and verification API stubs.
- Maintains existing room behavior tests while satisfying new DI requirements.

[student-lecture-room](frontend/src/app/student/student-lecture-room.ts)
Added in-room verification orchestration parallel to lecture list behavior.
- Injected `AuthService` and created form + state signals (`emailVerificationStatus`, `emailVerificationError`, `emailVerificationBusy`).
- Non-trivial methods `registerEmail`, `resendVerification`, and `resolveEmailVerificationErrorMessage` implement backend integration and user-facing error semantics.
- Preserves existing polling/submission flow while adding side-channel account verification actions.

[student-verify-email template](frontend/src/app/student/student-verify-email.html)
Introduced dedicated verification status view.
- Displays progress, explicit backend-driven error messages, and fallback login navigation action.
- Shows success state before redirect to student lectures.

[student-verify-email.spec](frontend/src/app/student/student-verify-email.spec.ts)
Added focused tests for verification deep-link behavior.
- Non-trivial scenarios cover successful token exchange redirect, missing token handling, and expired token error messaging.
- Uses mocked `ActivatedRoute` query param access and `AuthService.verifyStudentEmail` responses.

[student-verify-email](frontend/src/app/student/student-verify-email.ts)
Implemented the new student email verification route component.
- Class `StudentVerifyEmail` reads query token and executes token exchange through `AuthService.verifyStudentEmail`.
- Non-trivial `ngOnInit` flow maps backend error codes (`EMAIL_VERIFICATION_TOKEN_INVALID|CONSUMED|EXPIRED|REQUIRED`) to deterministic UX states.
- On success, updates state and navigates to `/student/lectures`; on failure, keeps user on actionable recovery path.

[Frontend Student Email Auth and Verification Integration Plan](plans/4-frontend-student-email-auth-and-verification-integration-plan.md)
Added a comprehensive implementation plan for student auth lifecycle integration.
- Defines contract mapping for anonymous/register/resend/verify endpoints and expected error codes.
- Breaks delivery into 8 iterations, with explicit acceptance criteria and test plan.
- Documents risks (token state split, deep-link loops, anti-enumeration leakage) and rollout ordering.
