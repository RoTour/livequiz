[pom.xml](backend/pom.xml)
Updated backend dependencies for email delivery support.
- Added `spring-boot-starter-mail` so `JavaMailSender` can be used by the SMTP verification adapter.

[LiveQuizProperties](backend/src/main/java/com/livequiz/backend/application/LiveQuizProperties.java)
Extended runtime configuration for student email verification.
- Added new properties for allowed domain, token TTL, resend cooldown, hourly cap, and verification URL base.
- Non-trivial logic is in the compact constructor, which applies safe defaults and bounds checks.

[SecurityConfig](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Updated authorization mapping for new student auth endpoints.
- `securityFilterChain(...)` now permits `/api/auth/anonymous` and `/api/auth/students/verify-email`.
- `securityFilterChain(...)` requires `ROLE_STUDENT` for `/api/auth/students/register-email` and `/api/auth/students/resend-verification`.

[JwtService](backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/JwtService.java)
Extended JWT generation and parsing for student lifecycle claims.
- Added `createStudentToken(...)` to force role `STUDENT` and carry `anonymous` and `emailVerified` claims.
- Refactored `createToken(...)` into an overload that accepts additional claims.
- Extended `TokenClaims` to include `anonymous` and `emailVerified` booleans.

[application.properties](backend/src/main/resources/application.properties)
Added student verification configuration keys.
- Introduced properties for allowed domain, verification TTL/cooldown/rate limits, verification URL base, SMTP toggle, and sender address.

[EmailVerificationChallengeService](backend/src/main/java/com/livequiz/backend/application/EmailVerificationChallengeService.java)
Introduced challenge issuance, throttling, and verification link generation.
- `issueFor(...)` creates a hashed-token challenge with TTL and dispatches mail before persisting challenge state.
- `ensureThrottleAllowsChallenge(...)` enforces resend cooldown and per-hour issuance caps.
- `buildVerificationUrl(...)` appends URL-encoded token to configured frontend URL.

[IssueAnonymousStudentTokenUseCase](backend/src/main/java/com/livequiz/backend/application/IssueAnonymousStudentTokenUseCase.java)
Added anonymous bootstrap for student identity.
- `execute()` creates a new `StudentIdentity` in `ANONYMOUS` state and returns a student JWT with `anonymous=true`.

[RegisterStudentEmailUseCase](backend/src/main/java/com/livequiz/backend/application/RegisterStudentEmailUseCase.java)
Added entrypoint for email registration on current student identity.
- `execute(...)` validates and normalizes email, preserves stable `studentId`, transitions identity to unverified, and issues verification challenge.
- Handles anti-enumeration by returning generic success when email belongs to another student.
- Handles DB uniqueness race (`DataIntegrityViolationException`) by returning generic success instead of surfacing 500.

[ResendStudentVerificationUseCase](backend/src/main/java/com/livequiz/backend/application/ResendStudentVerificationUseCase.java)
Added resend flow with optional email override.
- `execute(...)` can reuse stored email or accept a new validated email.
- Preserves generic response for ownership conflicts and race conditions.
- Returns `EMAIL_REQUIRED` when no email is available for resend.

[StudentEmailPolicy](backend/src/main/java/com/livequiz/backend/application/StudentEmailPolicy.java)
Centralized school-email validation policy.
- `normalizeAndValidate(...)` trims/lowercases input, checks regex format, and enforces exact configured domain match.
- Raises explicit API errors: `EMAIL_REQUIRED`, `EMAIL_INVALID_FORMAT`, `EMAIL_DOMAIN_NOT_ALLOWED`.

[StudentVerificationEmailSender](backend/src/main/java/com/livequiz/backend/application/StudentVerificationEmailSender.java)
Introduced delivery port for verification emails.
- Defines `sendVerificationEmail(...)` contract used by both logging and SMTP adapters.

[StudentVerificationTokenService](backend/src/main/java/com/livequiz/backend/application/StudentVerificationTokenService.java)
Added token utility service for verification flow.
- `generateOpaqueToken()` generates random opaque tokens.
- `hashToken(...)` computes SHA-256 hash for persistent lookup without storing raw token.

[VerifyStudentEmailUseCase](backend/src/main/java/com/livequiz/backend/application/VerifyStudentEmailUseCase.java)
Implemented token verification and identity upgrade flow.
- `execute(...)` validates token presence, token hash lookup, expiration and one-time use semantics.
- Consumes challenge, upgrades identity to `REGISTERED_VERIFIED`, and issues refreshed JWT (`anonymous=false`, `emailVerified=true`).
- Returns explicit error codes for invalid/consumed/expired token cases.

[EmailVerificationChallenge](backend/src/main/java/com/livequiz/backend/domain/student/EmailVerificationChallenge.java)
Added domain model for one-time email verification tokens.
- Constructor enforces invariants around IDs, token hash, and date ordering.
- `isExpiredAt(...)`, `isConsumed()`, and `consume(...)` model challenge lifecycle behavior.

[EmailVerificationChallengeRepository](backend/src/main/java/com/livequiz/backend/domain/student/EmailVerificationChallengeRepository.java)
Added domain repository contract for verification challenges.
- Exposes `save(...)`, `findLatestByTokenHash(...)`, and `findByStudentId(...)` operations.

[StudentIdentity](backend/src/main/java/com/livequiz/backend/domain/student/StudentIdentity.java)
Added domain model for stable student identity lifecycle.
- Constructor enforces state-dependent invariants between `status`, `email`, and `emailVerifiedAt`.
- `anonymous(...)`, `registerEmail(...)`, and `verifyEmail(...)` provide explicit lifecycle transitions.

[StudentIdentityRepository](backend/src/main/java/com/livequiz/backend/domain/student/StudentIdentityRepository.java)
Added domain repository for student identities.
- Defines lookup by `studentId` and `email`, plus persistence method.

[StudentIdentityStatus](backend/src/main/java/com/livequiz/backend/domain/student/StudentIdentityStatus.java)
Added student lifecycle state enum.
- Declares `ANONYMOUS`, `REGISTERED_UNVERIFIED`, and `REGISTERED_VERIFIED`.

[LoggingStudentVerificationEmailSender](backend/src/main/java/com/livequiz/backend/infrastructure/email/LoggingStudentVerificationEmailSender.java)
Added non-SMTP fallback delivery adapter.
- Activated by `livequiz.student-email-verification-smtp-enabled=false`.
- `sendVerificationEmail(...)` logs dispatch metadata without token-bearing URL.

[SmtpStudentVerificationEmailSender](backend/src/main/java/com/livequiz/backend/infrastructure/email/SmtpStudentVerificationEmailSender.java)
Added SMTP-backed delivery adapter.
- Activated by `livequiz.student-email-verification-smtp-enabled=true`.
- `sendVerificationEmail(...)` sends a plain-text verification message via `JavaMailSender`.

[InMemoryEmailVerificationChallengeRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryEmailVerificationChallengeRepository.java)
Added in-memory storage adapter for verification challenges.
- `findLatestByTokenHash(...)` returns latest matching challenge by `createdAt`.
- `findByStudentId(...)` returns most recent-first challenge history.

[InMemoryStudentIdentityRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryStudentIdentityRepository.java)
Added in-memory storage adapter for student identities.
- Maintains `studentId -> identity` and normalized `email -> studentId` maps.
- `save(...)` updates reverse email index when identity email changes.

[EmailVerificationChallengeEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/EmailVerificationChallengeEntity.java)
Added JPA entity for persisted verification challenges.
- Maps challenge fields (`challengeId`, `studentId`, `tokenHash`, expiry/consumed timestamps) to `email_verification_challenges`.

[JpaEmailVerificationChallengeRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaEmailVerificationChallengeRepository.java)
Added Spring Data repository for challenge queries.
- Declares methods to fetch latest by token hash and list by student with descending creation time.

[JpaPostgresEmailVerificationChallengeRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresEmailVerificationChallengeRepository.java)
Added Postgres adapter implementing domain challenge repository.
- Converts between domain `EmailVerificationChallenge` and JPA entity in `toEntity(...)`/`toDomain(...)`.

[JpaPostgresStudentIdentityRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresStudentIdentityRepository.java)
Added Postgres adapter implementing domain student identity repository.
- Maps domain state enum to string in persistence and back in `toEntity(...)`/`toDomain(...)`.

[JpaStudentIdentityRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaStudentIdentityRepository.java)
Added Spring Data repository for student identity entity.
- Adds `findByEmail(...)` lookup used by anti-enumeration and conflict handling logic.

[StudentIdentityEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/StudentIdentityEntity.java)
Added JPA entity for persisted student identities.
- Maps status/email/verification timestamps for table `student_identities`.

[StudentAuthController](backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/StudentAuthController.java)
Added student authentication endpoints.
- Exposes `POST /api/auth/anonymous`, `POST /api/auth/students/register-email`, `POST /api/auth/students/resend-verification`, and `POST /api/auth/students/verify-email`.
- Uses `CurrentUserService.requireUserId()` for authenticated student-bound operations.

[V2__add_student_identity_and_email_verification.sql](backend/src/main/resources/db/migration/V2__add_student_identity_and_email_verification.sql)
Added persistence migration for identity and challenge lifecycle.
- Creates `student_identities` with unique `email` constraint.
- Creates `email_verification_challenges` with FK to student identity and expiration check constraint.
- Adds indexes for status, token lookup, student recency, and active-window queries.

[StudentEmailAuthIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentEmailAuthIT.java)
Added integration test coverage for student email auth lifecycle.
- Validates anonymous token claims and endpoint access control.
- Verifies domain policy (`@ynov.com` allowed, other domains rejected).
- Covers end-to-end continuity of enrollment/submissions across anonymous -> verified transition.
- Covers verify-token failure modes (consumed and expired).
- Uses `CapturingStudentVerificationEmailSender` test bean to capture issued verification tokens.

[3-backend-student-email-auth-and-verification-plan.md](plans/3-backend-student-email-auth-and-verification-plan.md)
Added implementation plan document for this backend slice.
- Defines identity model, API contract, policy rules, rollout iterations, and done criteria.
