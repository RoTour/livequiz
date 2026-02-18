# AGENTS.md

Operational guide for coding agents working in this repository.

## Scope and Priority

- This file applies to the entire monorepo (`backend/`, `frontend/`).
- Follow explicit user instructions first, then this document.
- Preserve existing architecture and naming unless asked to refactor.

## External Agent Rules Check

- Cursor rules: none found (`.cursor/rules/`, `.cursorrules` not present).
- Copilot rules: none found (`.github/copilot-instructions.md` not present).
- If such files are added later, treat them as higher-priority repo instructions.

## Repository Overview

- Backend: Java 17, Spring Boot 4, Maven, JPA, Security, JWT.
- Frontend: Angular 21, TypeScript strict mode, Tailwind CSS, Vitest via Angular unit-test builder.
- Domain language is **Lecture-centric** (not Quiz aggregate): lecture, question, invite, enrollment, submission.

## Directory Map

- `backend/src/main/java/com/livequiz/backend/domain`: entities, value objects, repository interfaces.
- `backend/src/main/java/com/livequiz/backend/application`: use cases, app services, config properties.
- `backend/src/main/java/com/livequiz/backend/infrastructure`: web adapters, persistence adapters, jwt/security.
- `backend/src/test/java/...`: unit and integration tests (`*Test`, `*IT`).
- `frontend/src/app`: Angular app features, services, routes, guards, components.
- `README.md`, `ROADMAP.md`: product and delivery context.

## Build and Run Commands

### Backend (Maven)

- Install/compile only: `./mvnw -q -DskipTests compile`
- Run app: `./mvnw spring-boot:run`
- Unit tests: `./mvnw test`
- Integration tests + full verify: `./mvnw verify`
- Package jar: `./mvnw package`

### Frontend (Angular)

- Install deps: `bun isntall` (preferred)
- Dev server: `bun run start`
- Production build: `bun run build`
- Unit tests once: `bun run test`

## Single-Test Commands (Important)

### Backend single unit test

- One class: `./mvnw -Dtest=LectureTest test`
- One method: `./mvnw -Dtest=LectureTest#should_add_question_and_unlock_next_in_order test`

### Backend single integration test

- One IT class: `./mvnw -Dit.test=StudentFlowIT failsafe:integration-test failsafe:verify`
- One IT method: `./mvnw -Dit.test=StudentFlowIT#should_join_with_invite_and_submit_with_cooldown failsafe:integration-test failsafe:verify`

### Frontend single spec

- One spec file: `npm test -- --watch=false --include src/app/dashboard/dashboard.spec.ts`
- Optional pattern filter (if needed): `npm test -- --watch=false --testNamePattern="should create"`

## Profiles and Runtime Config

- Default backend profile is `postgres` (`application.properties`).
- In-memory profiles exist for local/testing: `in-memory`, `memory`.
- Required security env in non-in-memory contexts:
  - `LIVEQUIZ_JWT_SECRET` must be set and not insecure default.
- Domain config env vars:
  - `LIVEQUIZ_SUBMISSION_COOLDOWN_SECONDS`
  - `LIVEQUIZ_INVITE_BASE_URL`
  - `LIVEQUIZ_INVITE_EXPIRATION_HOURS`

## Architecture and Domain Conventions

- Keep DDD + Clean Architecture boundaries:
  - Domain: business invariants and core behavior.
  - Application: orchestration/use-case flow.
  - Infrastructure: controllers, persistence, security, external integrations.
- Prefer adding behavior to aggregates/value objects before pushing logic into controllers.
- Keep ubiquitous language stable:
  - `Lecture` is aggregate root.
  - Questions are lecture children.
  - Invites drive enrollment.
  - Student progression uses unlocked oldest unanswered question.

## Backend Code Style (Java)

- Java version: 17.
- Use constructor injection for Spring beans; avoid field injection.
- Keep methods focused; use early returns for guard clauses.
- Throw `IllegalArgumentException` for domain invariant violations.
- Throw `ApiException` (or specialized subclasses) for API-layer business errors.
- Return typed DTOs/records from controllers when shape is non-trivial.
- Keep endpoint paths and role requirements aligned with `SecurityConfig`.
- Avoid leaking internal exception messages in generic 500 responses.
- Prefer `Optional` in repository reads (`findBy...`) over nullable returns.

### Imports and formatting (Java)

- No wildcard imports.
- Group imports by package convention (project, java, framework).
- Preserve existing brace and indentation style (2-space indentation in current files).
- Keep line length readable; break long argument lists vertically.

### Naming (Java)

- Class names: `PascalCase` (`JoinLectureUseCase`).
- Methods/fields: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Endpoint DTO records: descriptive and scoped (`AddQuestionRequestDTO`).

## Frontend Code Style (Angular/TS)

- TypeScript strict mode is enabled; do not weaken strictness.
- Prefer explicit types for API contracts (`type` aliases in services).
- Use Angular signals where existing code uses signals.
- Keep UI orchestration in components; API wiring in services.
- Keep route auth logic in guard/interceptor, not duplicated in components.
- Use single quotes in TS files (per `.editorconfig`/Prettier settings).
- Follow Prettier settings (`printWidth: 100`, angular parser for HTML).

### Imports and formatting (TS)

- No unused imports.
- Prefer named imports; avoid namespace imports unless necessary.
- Preserve 2-space indentation.
- Keep template conditionals readable; avoid deeply nested control blocks.

### Naming (TS)

- Components/services/classes: `PascalCase`.
- Variables/functions: `camelCase`.
- File naming should follow existing Angular conventions used in repo.

## Error Handling Rules

- Backend:
  - Map business errors to structured API responses (`code`, `message`, `details`).
  - Use `429` for cooldown throttling and include retry metadata.
  - Enforce auth/role constraints through security config + identity service.
- Frontend:
  - Handle known API error statuses (especially `429`) and show actionable feedback.
  - Avoid swallowing unexpected errors silently.

## Security Rules

- Student identity must come from JWT subject when authenticated.
- Do not trust client-provided `studentId` over JWT identity.
- Instructor-only operations must remain instructor-guarded.
- Never commit secrets or sample production secrets.

## Testing Expectations for Changes

- Backend domain/use-case changes: run at least relevant unit tests.
- Backend endpoint/security changes: run integration tests (`verify` or targeted `*IT`).
- Frontend component/service changes: run frontend tests once (`--watch=false`).
- For cross-layer features, run both backend and frontend test suites.

## Agent Workflow Recommendations

- Read related domain + application + controller files before editing.
- Prefer minimal, local changes that match existing style.
- Update docs (`README.md`, `ROADMAP.md`) when behavior/contracts materially change.
- Do not create commits unless user explicitly asks.
- If tests fail, fix root cause rather than disabling tests.
