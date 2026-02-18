[README](README.md)
Lecture-centric product context was clarified.
 - Added explicit domain framing where `Lecture` is the aggregate root.
 - Documented implemented capabilities like JWT roles, invite enrollment, progression, and cooldown.

[ROADMAP](ROADMAP.md)
Roadmap was moved from quiz-centric milestones to lecture-centric delivery.
 - Reworked phases around lecture creation, invite enrollment, and student progression.
 - Added hardening phase for migrations, role-aware dashboards, and observability/rate limits.

[LivequizBackendApplication](backend/src/main/java/com/livequiz/backend/LivequizBackendApplication.java)
Configuration properties binding was enabled globally.
 - Added `@ConfigurationPropertiesScan` to auto-register `@ConfigurationProperties` classes like `LiveQuizProperties`.

[AddQuestionToLectureUseCase](backend/src/main/java/com/livequiz/backend/application/AddQuestionToLectureUseCase.java)
Introduced use case for lecture question creation.
 - `execute(...)` loads lecture, generates fallback UUID question IDs, calls `Lecture.addQuestion(...)`, then persists.
 - Uses `ApiException` with `LECTURE_NOT_FOUND` for missing aggregate.

[CreateLectureInviteUseCase](backend/src/main/java/com/livequiz/backend/application/CreateLectureInviteUseCase.java)
Added invite generation flow for instructors.
 - `execute(...)` validates lecture, generates active-unique 6-char codes, hashes token, creates invite with TTL.
 - Returns `CreateInviteResult` containing invite metadata and QR-friendly join URL.

[CurrentUserService](backend/src/main/java/com/livequiz/backend/application/CurrentUserService.java)
Centralized authenticated user resolution.
 - `requireUserId()` reads `SecurityContextHolder` and returns principal subject.
 - Converts invalid/absent auth into `ApiException(401, UNAUTHORIZED)`.

[GetLectureStateUseCase](backend/src/main/java/com/livequiz/backend/application/GetLectureStateUseCase.java)
Added read use case for current lecture state.
 - `execute(lectureId)` returns the aggregate or throws `LECTURE_NOT_FOUND`.

[GetNextQuestionForStudentUseCase](backend/src/main/java/com/livequiz/backend/application/GetNextQuestionForStudentUseCase.java)
Implemented student progression policy.
 - `execute(...)` enforces enrollment and selects oldest unlocked unanswered question by `Question.order`.
 - Returns `Optional<NextQuestionResult>` and throws `LECTURE_ENROLLMENT_REQUIRED` when needed.

[InviteTokenService](backend/src/main/java/com/livequiz/backend/application/InviteTokenService.java)
Added token/code generation utilities.
 - `generateOpaqueToken()` creates join token values.
 - `hashToken(...)` uses SHA-256 for secure invite lookup.
 - `generateJoinCode()` creates readable 6-char codes from a non-ambiguous alphabet.

[JoinLectureUseCase](backend/src/main/java/com/livequiz/backend/application/JoinLectureUseCase.java)
Implemented invite-based enrollment with idempotent behavior.
 - `execute(...)` accepts token or code, resolves active invite, validates lecture existence.
 - Uses `LectureEnrollmentRepository.findByLectureIdAndStudentId(...)` to return stable `enrolledAt` on repeat joins.

[ListLectureInvitesUseCase](backend/src/main/java/com/livequiz/backend/application/ListLectureInvitesUseCase.java)
Added invite listing use case.
 - `execute(lectureId)` validates lecture then returns invite list from repository.

[LiveQuizProperties](backend/src/main/java/com/livequiz/backend/application/LiveQuizProperties.java)
Introduced typed runtime configuration.
 - Encapsulates `submissionCooldownSeconds`, `inviteBaseUrl`, and `inviteExpirationHours`.
 - Constructor enforces safe defaults and max invite TTL of 24h.

[RevokeLectureInviteUseCase](backend/src/main/java/com/livequiz/backend/application/RevokeLectureInviteUseCase.java)
Added invite revocation with lecture scoping.
 - `execute(lectureId, inviteId)` checks invite existence and lecture path match.
 - Uses immutable revoke via `LectureInvite.revoke(...)` and persists updated invite.

[SubmitAnswerUseCase](backend/src/main/java/com/livequiz/backend/application/SubmitAnswerUseCase.java)
Implemented secure answer submission pipeline.
 - `execute(...)` validates lecture, enrollment, unlocked state, cooldown, and appends a submission attempt.
 - `enforceCooldown(...)` computes retry window and throws `SubmissionCooldownException` (`429`).

[UnlockNextQuestionUseCase](backend/src/main/java/com/livequiz/backend/application/UnlockNextQuestionUseCase.java)
Added convenience unlock progression.
 - `execute(lectureId)` delegates to aggregate `unlockNextQuestion()` and saves result.

[UnlockQuestionUseCase](backend/src/main/java/com/livequiz/backend/application/UnlockQuestionUseCase.java)
Added targeted question unlock use case.
 - `execute(lectureId, questionId)` fetches lecture, unlocks selected question, persists.

[KeyPoint](backend/src/main/java/com/livequiz/backend/domain/lecture/KeyPoint.java)
New value object for expected answer key points.
 - Validates concept text and supports optional explanation.

[Lecture](backend/src/main/java/com/livequiz/backend/domain/lecture/Lecture.java)
Lecture aggregate was expanded significantly.
 - Added state: `questions` and `unlockedQuestionIds` with constructor invariants (uniqueness and consistency).
 - Added domain behavior: `addQuestion(...)`, `unlockQuestion(...)`, and `unlockNextQuestion()` with deterministic order.

[LectureEnrollment](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureEnrollment.java)
New enrollment record was introduced.
 - Captures `(lectureId, studentId, enrolledAt)` and validates all fields.

[LectureEnrollmentRepository](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureEnrollmentRepository.java)
Enrollment repository contract was created.
 - Adds `save`, `existsByLectureIdAndStudentId`, and `findByLectureIdAndStudentId` for idempotent join flows.

[LectureInvite](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureInvite.java)
Invite domain model was introduced.
 - Enforces required fields and TTL (`MAX_TTL = 24h`).
 - Provides `isActiveAt(...)` and immutable `revoke(...)` behavior.

[LectureInviteRepository](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureInviteRepository.java)
Invite repository contract was introduced.
 - Supports lookups by ID, active token hash, active join code, and active code collision checks.

[LectureRepository](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureRepository.java)
Lecture repository port was extended.
 - Added `findById(LectureId)` for read-modify-write use cases.

[Question](backend/src/main/java/com/livequiz/backend/domain/lecture/Question.java)
Question entity was added to lecture domain.
 - Includes fields for prompt, model answer, time limit, order, and key points.
 - Constructor validates ID, text fields, positive time limit, and positive order.

[QuestionId](backend/src/main/java/com/livequiz/backend/domain/lecture/QuestionId.java)
Added value object for question identity.
 - Validates non-empty IDs.

[Feedback](backend/src/main/java/com/livequiz/backend/domain/submission/Feedback.java)
Submission feedback value object was introduced.
 - Normalizes nullable collections and comment values.

[Submission](backend/src/main/java/com/livequiz/backend/domain/submission/Submission.java)
Submission domain entity was added.
 - Captures attempt metadata for cooldown/history behavior.
 - Includes `provideFeedback(...)` for later assessment lifecycle.

[SubmissionId](backend/src/main/java/com/livequiz/backend/domain/submission/SubmissionId.java)
Added submission identity value object.
 - Validates non-empty IDs.

[SubmissionRepository](backend/src/main/java/com/livequiz/backend/domain/submission/SubmissionRepository.java)
Submission repository contract was introduced.
 - Adds append/save, latest-attempt lookup, and submitted-question projection per lecture/student.

[InMemoryLectureEnrollmentRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryLectureEnrollmentRepository.java)
In-memory enrollment adapter was added.
 - Uses `lectureId::studentId` keying for `save/exists/find` operations.
 - Supports both `in-memory` and `memory` profiles.

[InMemoryLectureInviteRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryLectureInviteRepository.java)
In-memory invite adapter was added.
 - Implements active invite lookup by token hash/code and active-code collision checks.
 - Supports lecture-scoped listing sorted by `createdAt` descending.

[InMemoryLectureRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryLectureRepository.java)
In-memory lecture adapter was updated.
 - Added `findById(...)` support and profile compatibility (`in-memory`, `memory`).

[InMemorySubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemorySubmissionRepository.java)
In-memory submission adapter was introduced.
 - Stores append-only attempts.
 - Implements latest-attempt retrieval and submitted-question set projection.

[JpaLectureEnrollmentRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaLectureEnrollmentRepository.java)
Spring Data enrollment repository was added.
 - Provides JPA CRUD for `LectureEnrollmentEntity` with composite key.

[JpaLectureInviteRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaLectureInviteRepository.java)
Spring Data invite repository was added.
 - Adds derived queries for active token/code lookup and active code uniqueness checks.

[JpaPostgresLectureEnrollmentRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureEnrollmentRepository.java)
Postgres adapter for enrollments was introduced.
 - Maps between `LectureEnrollment` and `LectureEnrollmentEntity`.
 - Implements existence and full lookup by `(lectureId, studentId)`.

[JpaPostgresLectureInviteRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureInviteRepository.java)
Postgres adapter for invites was introduced.
 - Handles domain/entity mapping for save/find/list.
 - Implements active invite lookups and active code existence checks.

[JpaPostgresLectureRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureRepository.java)
Postgres lecture persistence was reworked around lecture aggregate.
 - Persists embedded questions and unlocked question IDs.
 - Rehydrates full `Lecture` with ordered `Question` children in `findById(...)`.

[JpaPostgresQuizRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresQuizRepository.java)
Obsolete quiz repository adapter was removed.
 - Deletion aligns persistence layer with lecture-centric domain language.

[JpaPostgresSubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresSubmissionRepository.java)
Postgres submission adapter was added.
 - Maps `Submission` to `SubmissionEntity` and supports latest-attempt retrieval.
 - Implements submitted-question projection via repository query.

[JpaSubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaSubmissionRepository.java)
Spring Data submission repository was added.
 - Includes `findTopBy...OrderBySubmittedAtDesc` for cooldown checks.
 - Defines JPQL distinct query for submitted question IDs.

[LectureEnrollmentEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEnrollmentEntity.java)
New JPA entity for lecture enrollment table.
 - Uses embedded key `LectureEnrollmentId` and `enrolledAt` timestamp.

[LectureEnrollmentId](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEnrollmentId.java)
Composite key class was added.
 - Implements explicit `equals/hashCode` for stable JPA identity semantics.

[LectureEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEntity.java)
Lecture persistence model was extended.
 - Added `@ElementCollection` mappings for `lecture_questions` and `lecture_unlocked_questions`.
 - Added constructor/getters needed by adapter mappings.

[LectureInviteEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureInviteEntity.java)
Invite persistence entity was introduced.
 - Stores join code, token hash, lifecycle timestamps, and creator metadata.

[LectureQuestionEmbeddable](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureQuestionEmbeddable.java)
Question persistence component was introduced.
 - Encodes question payload fields including `questionOrder` and time limit.

[SubmissionEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/SubmissionEntity.java)
Submission persistence entity was introduced.
 - Stores append-only attempts in `submission_attempts` table.

[ApiException](backend/src/main/java/com/livequiz/backend/infrastructure/web/ApiException.java)
Unified API exception type was introduced.
 - Carries both machine-readable `code` and `HttpStatus` for error handlers.

[ErrorResponse](backend/src/main/java/com/livequiz/backend/infrastructure/web/ErrorResponse.java)
Error payload contract was enhanced.
 - Added `details` map for structured metadata (for example `retryAfterSeconds`).

[GlobalExceptionHandler](backend/src/main/java/com/livequiz/backend/infrastructure/web/GlobalExceptionHandler.java)
Exception mapping was hardened.
 - Added dedicated handlers for `ApiException` and `SubmissionCooldownException`.
 - Generic 500 no longer leaks internal exception messages.

[LectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureController.java)
Instructor lecture API was expanded.
 - Added endpoints/methods: `addQuestion(...)`, `unlockQuestion(...)`, `unlockNextQuestion(...)`, `getLectureState(...)`.
 - Added DTO/response records including ordered question state with unlocked flag.

[LectureInviteController](backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureInviteController.java)
Instructor invite management API was added.
 - Endpoints create/list/revoke invites under `/api/lectures/{lectureId}/invites`.
 - Uses `CurrentUserService.requireUserId()` and maps active status in response records.

[SecurityConfig](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Security was migrated to stateless role-based JWT enforcement.
 - Disabled session/form/basic auth; configured route-level role checks for instructor/student paths.
 - Added in-memory users `instructor/password` and `student/password` with corresponding roles.

[StudentLectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/StudentLectureController.java)
Student workflow API was added.
 - Added methods: `joinLecture(...)`, `getNextQuestion(...)`, and `submitAnswer(...)`.
 - Uses JWT subject via `CurrentUserService`; next-question endpoint returns `hasQuestion` envelope.

[SubmissionCooldownException](backend/src/main/java/com/livequiz/backend/infrastructure/web/SubmissionCooldownException.java)
Specialized cooldown exception was introduced.
 - Encapsulates `429 SUBMISSION_COOLDOWN` and `retryAfterSeconds` payload data.

[JwtAuthenticationFilter](backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/JwtAuthenticationFilter.java)
JWT filter was upgraded to role-aware claims handling.
 - Uses `JwtService.TokenClaims` and rejects invalid/blank subject claims.
 - Maps role claim into `ROLE_*` authorities (default `STUDENT`).

[JwtController](backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/JwtController.java)
Login token issuance was improved.
 - Derives role from authenticated authorities and passes it to `createToken(...)`.
 - Converts login failures into structured `INVALID_CREDENTIALS` API errors.

[JwtService](backend/src/main/java/com/livequiz/backend/infrastructure/web/jwt/JwtService.java)
JWT service was refactored for secure, configurable secrets and role claims.
 - Constructor validates `livequiz.jwt.secret` and rejects unsafe/missing values for non-in-memory profiles.
 - `createToken(subject, role)` writes role claim; `validateToken(...)` returns `TokenClaims`.

[application-in-memory.properties](backend/src/main/resources/application-in-memory.properties)
In-memory profile runtime defaults were introduced.
 - Disables datasource/JPA auto-config.
 - Adds local JWT secret for test/local startup.

[application-memory.properties](backend/src/main/resources/application-memory.properties)
Legacy memory profile runtime defaults were introduced.
 - Mirrors in-memory behavior for compatibility.

[application.properties](backend/src/main/resources/application.properties)
Main runtime config gained new domain/security knobs.
 - Added cooldown and invite config bindings (`LIVEQUIZ_*`).
 - Added `livequiz.jwt.secret` binding.

[LivequizBackendApplicationIT](backend/src/test/java/com/livequiz/backend/LivequizBackendApplicationIT.java)
Context bootstrap test was aligned with in-memory profile.
 - Added `@ActiveProfiles("in-memory")` to avoid DB/JPA dependency in this IT.

[LectureTest](backend/src/test/java/com/livequiz/backend/domain/lecture/LectureTest.java)
Domain unit tests were migrated and expanded for lecture aggregate.
 - Covers title invariant, `unlockNextQuestion()` order progression, and duplicate question ID rejection.

[QuizTest](backend/src/test/java/com/livequiz/backend/domain/quiz/QuizTest.java)
Obsolete quiz domain unit test was removed.
 - Replaced by lecture-domain coverage in `LectureTest`.

[QuizControllerIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/QuizControllerIT.java)
Integration test was adapted to lecture endpoints with auth.
 - Adds helper `loginAsInstructor()` and tests lecture creation validation under JWT.
 - Keeps 404 unknown endpoint assertion in authenticated context.

[StudentFlowIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java)
New end-to-end integration test suite was introduced.
 - Tests join via invite, idempotent join behavior, progression, submission cooldown (`429`) and no-next-question outcome.
 - Adds revoke negative case for lecture/invite path mismatch.

[app.html](frontend/src/app/app.html)
Root shell was simplified.
 - Removed direct quiz UI and static status text, keeping backend status and router outlet.

[app.spec.ts](frontend/src/app/app.spec.ts)
Root test was updated to match shell-only app.
 - Asserts presence of `<router-outlet>` instead of removed title heading.

[app.ts](frontend/src/app/app.ts)
Root component orchestration was reduced.
 - Removed missing quiz component import and manual health boot logic.
 - Keeps app as routing shell with backend status component.

[dashboard.html](frontend/src/app/dashboard/dashboard.html)
Dashboard UI was fully implemented.
 - Added instructor sections for lecture creation, question management, unlock actions, and invite generation.
 - Added student sections for join, next-question fetch, submission, cooldown messaging, and lecture state display.

[dashboard.ts](frontend/src/app/dashboard/dashboard.ts)
Dashboard behavior/controller was fully implemented.
 - Added non-trivial async methods: `createLecture`, `addQuestion`, `unlockNextQuestion`, `createInvite`, `joinLecture`, `loadNextQuestion`, `submitAnswer`, `refreshLectureState`.
 - Uses Angular signals + reactive forms; handles 429 cooldown using `error.details.retryAfterSeconds`.

[lecture.service.ts](frontend/src/app/lecture.service.ts)
Frontend API service was expanded to full lecture lifecycle.
 - Added methods for question management, state read, invite create, join, next-question, and submit.
 - Added explicit TypeScript API contracts (`LectureStateResponse`, `CreateInviteResponse`, `NextQuestionResponse`, etc.).

[auth-guard.ts](frontend/src/app/login/auth-guard.ts)
Auth guard was corrected.
 - Uses signal invocation `authService.token()` instead of signal object reference.
 - Redirect path updated to `/auth/login`.

[login.ts](frontend/src/app/login/login.ts)
Login form behavior was corrected.
 - Default creds align with current backend user (`instructor/password`).
 - Uses entered password in login call instead of hardcoded string.
