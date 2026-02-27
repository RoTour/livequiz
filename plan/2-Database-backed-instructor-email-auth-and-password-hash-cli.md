# 2 - Database-backed instructor email auth and password hash CLI plan

## Context

Instructor authentication currently relies on in-memory user definitions in `SecurityConfig`, including plaintext development passwords in code. Teacher role classification is already available via `teacher_identities`, but login credentials are not persisted.

New requirement: instructors must authenticate with email-based accounts whose hashed passwords are stored in database, and developers need a simple CLI tool to generate password hashes for setup.

## Goal

Introduce email-based instructor account authentication backed by persisted password hashes, while preserving existing student flows and providing a developer-friendly password hash generation CLI.

Success criteria:
- Instructor authentication uses email + hashed password from DB in `postgres` profile.
- No plaintext instructor password constants remain in runtime auth configuration.
- Dev team can generate bcrypt hashes quickly through a documented CLI command.
- Teacher classification continues to gate `INSTRUCTOR` role assignment.

## Scope and Non-goals

In scope:
- Backend persistence model for instructor auth accounts (email + password hash + active flag).
- Security wiring for DB-backed instructor auth in `postgres` profile.
- Email-based login contract support.
- Password hash CLI helper script/tooling for local setup.
- Documentation updates for setup and instructor provisioning.
- Regression and integration tests updates.

Non-goals:
- Admin UI/API for managing instructor accounts.
- Password reset workflow.
- Student login model redesign.
- External IdP/OAuth migration.

## Design principles

- Email is the only instructor credential identifier.
- Least privilege and explicit activation: only active instructor auth accounts can authenticate.
- Keep DDD + Clean Architecture boundaries:
  - Domain: instructor auth account model and repository contract.
  - Application: role resolution and auth orchestration remains framework-agnostic.
  - Infrastructure: JPA, Spring Security adapters, CLI shell/tool.
  - Interface: auth endpoint contract and docs.
- Keep rollout safe across profiles:
  - `postgres`: DB-backed instructor auth.
  - `in-memory`/`memory`: deterministic test credentials for existing flows.

## Iterations

- [Iteration 01 - Instructor auth domain and persistence contract]
  - Type: backend
  - Why: Establish explicit domain boundary for instructor credentials.
  - Domain: Add `InstructorAccount` model with normalized email, password hash, active state invariants; add repository abstraction.
  - Application: Keep role classification service unchanged except principal now expected as email.
  - Infrastructure: No runtime wiring yet; compile-only scaffolding.
  - Interface: No API changes yet.
  - Risks/open questions: Email normalization consistency across auth and teacher classification.
  - Acceptance criteria:
    - Domain model validates email and hash presence.
    - Repository abstraction supports lookup by email.

- [Iteration 02 - Postgres instructor account persistence]
  - Type: backend
  - Why: Persist hashed instructor credentials in DB.
  - Domain: Reuse iteration 01 contracts.
  - Application: No orchestration changes.
  - Infrastructure: Add Flyway migration for `instructor_accounts` (email PK, password_hash, active, timestamps), JPA entity/repository/adapter.
  - Interface: No endpoint changes.
  - Risks/open questions: Existing environments need explicit account setup after migration.
  - Acceptance criteria:
    - Migration applies cleanly.
    - Adapter can read active instructor accounts by email.

- [Iteration 03 - Security profile wiring for DB-backed instructor auth]
  - Type: backend
  - Why: Replace hardcoded instructor credentials for `postgres` runtime.
  - Domain: No new domain behavior.
  - Application: No new use case.
  - Infrastructure: Add `UserDetailsService` adapter for `postgres` backed by instructor accounts and bcrypt hashes; keep in-memory manager only for `in-memory`/`memory` profiles.
  - Interface: Auth endpoint behavior unchanged except identifier semantics.
  - Risks/open questions: Backward compatibility with clients still posting `username` key.
  - Acceptance criteria:
    - Postgres auth resolves instructors from DB email accounts.
    - In-memory profile tests remain deterministic.

- [Iteration 04 - Email-first login contract and teacher classification alignment]
  - Type: fullstack
  - Why: Align auth request semantics and teacher role classification to email principals.
  - Domain: Teacher identity values updated to email format.
  - Application: Role resolution continues to use normalized principal value.
  - Infrastructure: Update login request parsing and test fixtures from username-style instructor identifiers to emails.
  - Interface: Frontend login flow labels and payload become email-first.
  - Risks/open questions: Preserve compatibility for older clients during transition.
  - Acceptance criteria:
    - Instructor logins succeed with email credentials.
    - Teacher classification still differentiates eligible instructor vs fallback student.

- [Iteration 05 - Password hash generation CLI]
  - Type: backend
  - Why: Reduce setup friction and avoid manual hash generation mistakes.
  - Domain: No domain changes.
  - Application: No application changes.
  - Infrastructure: Add CLI class and shell wrapper that outputs bcrypt hash for a provided password.
  - Interface: Documented terminal command usage for developers.
  - Risks/open questions: Prevent accidental shell history leaks by offering stdin/interactive mode.
  - Acceptance criteria:
    - Dev can run one command to generate bcrypt hash.
    - Output format is directly insertable into `instructor_accounts.password_hash`.

- [Iteration 06 - Tests, docs, and rollout hardening]
  - Type: fullstack
  - Why: Ensure reliable adoption and prevent regressions.
  - Domain: No new domain behavior.
  - Application: Validate role resolution with email principals.
  - Infrastructure: Update backend integration tests and frontend auth/login tests for email-based instructor login semantics.
  - Interface: Update `README.md` with full setup instructions and teacher account provisioning including CLI usage.
  - Risks/open questions: Documentation drift vs runtime defaults.
  - Acceptance criteria:
    - Backend and frontend test suites pass.
    - README includes end-to-end setup and instructor onboarding with hash CLI.
