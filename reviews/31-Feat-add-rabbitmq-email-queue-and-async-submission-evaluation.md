[ROADMAP](ROADMAP.md)
Updated roadmap tracking for asynchronous processing rollout.
 - Added Phase 9 for RabbitMQ-backed async processing and linked `plans/5-rabbitmq-email-and-submission-processing-plan.md`.
 - Marked implemented checklist items (topology, outbox, queued email, async evaluation) and left queue IT coverage pending.

[docker-compose](backend/docker-compose.yml)
Extended local runtime topology with RabbitMQ.
 - Added `rabbitmq:3.13-management` service with AMQP (`5672`) and management UI (`15672`) ports.
 - Kept existing Postgres service and named volume wiring unchanged.

[pom](backend/pom.xml)
Extended backend dependencies for messaging.
 - Added `spring-boot-starter-amqp` so RabbitTemplate, listeners, and queue declarations are available.
 - Preserved existing Spring Boot, JPA, Flyway, Security, Mail, and JWT dependencies.

[LivequizBackendApplication](backend/src/main/java/com/livequiz/backend/LivequizBackendApplication.java)
Enabled scheduled background processing.
 - Added `@EnableScheduling` so periodic outbox polling can run.
 - Kept `@ConfigurationPropertiesScan` and application bootstrap flow intact.

[EmailVerificationChallengeService](backend/src/main/java/com/livequiz/backend/application/EmailVerificationChallengeService.java)
Refactored email verification issuance to use dispatch abstraction.
 - `issueFor(...)` is now transactional and persists challenge before dispatching through `StudentVerificationEmailDispatchService`.
 - Preserved throttling logic in `ensureThrottleAllowsChallenge(...)` and URL generation in `buildVerificationUrl(...)`.

[GetStudentAnswerStatusesUseCase](backend/src/main/java/com/livequiz/backend/application/GetStudentAnswerStatusesUseCase.java)
Adjusted read model to expose persisted evaluation status.
 - `execute(...)` now maps `submission.answerStatus()` instead of hardcoded awaiting status.
 - Enrollment guard (`ensureStudentIsEnrolled(...)`) and question ordering remain unchanged.

[SubmitAnswerUseCase](backend/src/main/java/com/livequiz/backend/application/SubmitAnswerUseCase.java)
Converted submit flow to async evaluation trigger.
 - `execute(...)` became transactional and now calls `SubmissionEvaluationDispatchService.dispatch(...)` after persistence.
 - Retained existing guards: `ensureStudentIsEnrolled(...)`, `ensureQuestionIsUnlocked(...)`, `enforceCooldown(...)`.

[Submission](backend/src/main/java/com/livequiz/backend/domain/submission/Submission.java)
Expanded submission aggregate state for async evaluation lifecycle.
 - Added fields for `answerStatus`, `evaluationCompletedAt`, and persisted `Feedback`.
 - Introduced overloaded constructor for hydrated entities and strict status validation via `ALLOWED_STATUSES`.
 - Added `applyEvaluation(...)` to atomically update status, completion time, and feedback payload.

[SubmissionRepository](backend/src/main/java/com/livequiz/backend/domain/submission/SubmissionRepository.java)
Extended domain persistence contract.
 - Added `findById(SubmissionId)` for worker-side lookup during async evaluation.
 - Kept query APIs for latest submission, counts, and instructor analytics projections.

[InMemorySubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemorySubmissionRepository.java)
Updated in-memory behavior to support updates and direct lookup.
 - `save(...)` now replaces existing rows by submission ID before adding.
 - Added `findById(...)` used by async evaluation consumers.

[JpaPostgresSubmissionRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresSubmissionRepository.java)
Extended JPA adapter to persist evaluation metadata.
 - `save(...)` now writes status/completion/feedback columns.
 - Added `findById(...)` and centralized mapping in `toDomain(...)`.
 - Introduced `mapFeedback(...)`, `serializeMissingKeyPoints(...)`, and `deserializeMissingKeyPoints(...)` helpers.

[SubmissionEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/SubmissionEntity.java)
Extended submission table mapping to new columns.
 - Added `answerStatus`, `evaluationCompletedAt`, `feedbackIsCorrect`, `feedbackComment`, and `feedbackMissingKeyPoints`.
 - Updated constructor/getters to fully hydrate persistence state.

[application-in-memory](backend/src/main/resources/application-in-memory.properties)
Configured messaging defaults for in-memory profile.
 - Added `livequiz.messaging.enabled=false` to keep tests and local in-memory runs synchronous.

[application-memory](backend/src/main/resources/application-memory.properties)
Configured messaging defaults for memory profile.
 - Added `livequiz.messaging.enabled=false` to align with in-memory profile behavior.

[application-postgres](backend/src/main/resources/application-postgres.properties)
Added RabbitMQ connection settings for Postgres profile.
 - Added `spring.rabbitmq.host/port/username/password` with env-var based defaults.

[application](backend/src/main/resources/application.properties)
Added queue, worker, and quota runtime configuration surface.
 - Introduced `livequiz.messaging.*` keys for exchange, queue names, routing keys, attempts, concurrency, and outbox batching.
 - Added SES limit keys `livequiz.email.ses.max-per-second` and `livequiz.email.ses.max-per-day`.

[AnswerEvaluationProvider](backend/src/main/java/com/livequiz/backend/application/AnswerEvaluationProvider.java)
Introduced application port for answer evaluation.
 - Defines `evaluate(prompt, modelAnswer, answerText)` to decouple use cases/workers from evaluator implementation.
 - Added `EvaluationResult` record with `AnswerEvaluationStatus` and `Feedback`.

[EmailDailyQuotaUsageRepository](backend/src/main/java/com/livequiz/backend/application/messaging/EmailDailyQuotaUsageRepository.java)
Added quota reservation abstraction.
 - `tryReserve(...)` centralizes daily SES cap logic away from consumers.

[EmailDispatchJob](backend/src/main/java/com/livequiz/backend/application/messaging/EmailDispatchJob.java)
Introduced domain-style record for email delivery jobs.
 - Includes lifecycle transitions `markProcessing(...)`, `markSent(...)`, `markRetryScheduled(...)`, `defer(...)`, `markFailedFinal(...)`.
 - Validates core invariants in canonical constructor (IDs, timestamps, status).

[EmailDispatchJobRepository](backend/src/main/java/com/livequiz/backend/application/messaging/EmailDispatchJobRepository.java)
Defined repository contract for email dispatch state machine.
 - Added `claimForProcessing(...)` to support idempotent consumer locking.

[EmailDispatchRequestedPayload](backend/src/main/java/com/livequiz/backend/application/messaging/EmailDispatchRequestedPayload.java)
Added payload DTO for email dispatch event.
 - Captures message ID, recipient, token, verification URL, and expiration timestamp.

[EmailDispatchStatus](backend/src/main/java/com/livequiz/backend/application/messaging/EmailDispatchStatus.java)
Defined email job statuses.
 - Added `PROCESSING` in addition to `QUEUED`, `SENT`, `RETRY_SCHEDULED`, `FAILED_FINAL`.

[LiveQuizMessagingProperties](backend/src/main/java/com/livequiz/backend/application/messaging/LiveQuizMessagingProperties.java)
Added strongly typed configuration for messaging.
 - Nested records `Email`, `Submission`, and `Outbox` expose topology, retry, concurrency, and polling settings.
 - Constructors enforce fallback defaults when values are missing or invalid.

[MessageEnvelope](backend/src/main/java/com/livequiz/backend/application/messaging/MessageEnvelope.java)
Added generic envelope for all event payloads.
 - Standardizes metadata (`messageId`, `eventType`, `schemaVersion`, `correlationId`, `causationId`, `occurredAt`).

[OutboxMessage](backend/src/main/java/com/livequiz/backend/application/messaging/OutboxMessage.java)
Introduced outbox domain record.
 - Includes immutable transition methods `markPublished(...)` and `markPublishFailed(...)`.
 - Validates required fields and non-negative attempt count.

[OutboxMessageRepository](backend/src/main/java/com/livequiz/backend/application/messaging/OutboxMessageRepository.java)
Defined outbox persistence operations.
 - Added `findUnpublishedByIdForUpdate(...)` to support publisher-side row locking.

[StudentVerificationEmailDispatchService](backend/src/main/java/com/livequiz/backend/application/messaging/StudentVerificationEmailDispatchService.java)
Added dispatch abstraction for verification email flow.
 - Separates use-case orchestration from direct SMTP vs queued Rabbit behavior.

[SubmissionEvaluationDispatchService](backend/src/main/java/com/livequiz/backend/application/messaging/SubmissionEvaluationDispatchService.java)
Added dispatch abstraction for answer evaluation jobs.
 - Enables profile-based no-op or Rabbit producer implementations.

[SubmissionEvaluationJob](backend/src/main/java/com/livequiz/backend/application/messaging/SubmissionEvaluationJob.java)
Introduced evaluation job state record.
 - Implements lifecycle transitions `markProcessing(...)`, `markRetryScheduled(...)`, `markCompleted(...)`, `markFailedFinal(...)`.
 - Enforces identifiers/status/timestamp invariants in constructor.

[SubmissionEvaluationJobRepository](backend/src/main/java/com/livequiz/backend/application/messaging/SubmissionEvaluationJobRepository.java)
Defined repository contract for evaluation jobs.
 - Added `claimForProcessing(...)` to protect against duplicate worker processing.

[SubmissionEvaluationJobStatus](backend/src/main/java/com/livequiz/backend/application/messaging/SubmissionEvaluationJobStatus.java)
Added evaluation lifecycle enum.
 - Tracks `QUEUED`, `PROCESSING`, `RETRY_SCHEDULED`, `FAILED_FINAL`, `COMPLETED`.

[SubmissionEvaluationRequestedPayload](backend/src/main/java/com/livequiz/backend/application/messaging/SubmissionEvaluationRequestedPayload.java)
Added payload DTO for submission evaluation events.
 - Includes submission, lecture, question, and student identity fields.

[DeterministicAnswerEvaluationProvider](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/DeterministicAnswerEvaluationProvider.java)
Provided baseline evaluator implementation for async pipeline.
 - `evaluate(...)` classifies answers into `INCOMPLETE`, `CORRECT`, or `INCORRECT` via simple normalization and matching.
 - `normalize(...)` ensures case/whitespace-insensitive comparisons.

[DirectStudentVerificationEmailDispatchService](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/DirectStudentVerificationEmailDispatchService.java)
Added non-messaging fallback dispatch implementation.
 - Under `livequiz.messaging.enabled=false`, `dispatch(...)` calls `StudentVerificationEmailSender` directly.

[EmailDispatchConsumer](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/EmailDispatchConsumer.java)
Implemented asynchronous email worker with idempotency and quota control.
 - `consume(...)` performs message ID parse, atomic job claim, quota reservation, per-second throttling, send, and state transitions.
 - `handleProcessingFailure(...)` classifies retry/final-failure paths and routes to retry/DLQ.
 - Uses helper methods `reserveDailyQuota(...)`, `republishWithDelay(...)`, `waitForRateLimit(...)`, `readAttempt(...)`, `nextUtcMidnight(...)`.

[NoopSubmissionEvaluationDispatchService](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/NoopSubmissionEvaluationDispatchService.java)
Added no-op dispatch implementation for non-messaging profiles.
 - `dispatch(...)` intentionally does nothing to preserve synchronous local mode.

[OutboxPublishWorker](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/OutboxPublishWorker.java)
Added per-message publisher worker with isolated transaction.
 - `publishSingle(...)` runs in `REQUIRES_NEW`, locks unpublished row, publishes message, then marks success/failure.
 - Encapsulates durable AMQP metadata assignment and attempt progression.

[OutboxPublisher](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/OutboxPublisher.java)
Added scheduled outbox scanner/orchestrator.
 - `publishPending(...)` fetches pending batch and delegates each ID to `OutboxPublishWorker.publishSingle(...)`.

[RabbitMqTopologyConfiguration](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/RabbitMqTopologyConfiguration.java)
Implemented full queue topology declaration.
 - Declares `TopicExchange`, main/retry/DLQ queues, and bindings for both email and submission flows.
 - Applies DLX/DLK arguments to main and retry queues to support retry and dead-letter routing.

[RabbitStudentVerificationEmailDispatchService](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/RabbitStudentVerificationEmailDispatchService.java)
Implemented Rabbit producer for verification emails.
 - `dispatch(...)` creates `EmailDispatchJob`, serializes `MessageEnvelope<EmailDispatchRequestedPayload>`, and inserts outbox row.
 - `toJson(...)` centralizes serialization and hard-fails on payload encoding errors.

[RabbitSubmissionEvaluationDispatchService](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/RabbitSubmissionEvaluationDispatchService.java)
Implemented Rabbit producer for submission evaluation.
 - `dispatch(...)` creates `SubmissionEvaluationJob`, wraps payload in envelope, and persists outbox event.
 - `toJson(...)` handles payload serialization consistently with email producer.

[RetryBackoffPolicy](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/RetryBackoffPolicy.java)
Added centralized exponential-ish backoff definitions.
 - Exposes `emailDelayForAttempt(...)` and `submissionDelayForAttempt(...)` with bounded delay arrays.

[SubmissionEvaluationConsumer](backend/src/main/java/com/livequiz/backend/infrastructure/messaging/SubmissionEvaluationConsumer.java)
Implemented asynchronous submission evaluation worker.
 - `consume(...)` claims job, loads submission+lecture context, resolves question, evaluates answer, persists status/feedback, and marks completion.
 - `handleProcessingFailure(...)` handles retry scheduling and DLQ finalization using persisted attempt state.
 - Includes helpers `republishWithDelay(...)`, `readAttempt(...)`, and `readIdentifiers(...)`.

[InMemoryEmailDailyQuotaUsageRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryEmailDailyQuotaUsageRepository.java)
Added in-memory quota store for non-DB profiles.
 - `tryReserve(...)` uses synchronized increment with max-per-day check.

[InMemoryEmailDispatchJobRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryEmailDispatchJobRepository.java)
Added in-memory email job repository.
 - Supports read/write plus `claimForProcessing(...)` status transition guard.

[InMemoryOutboxMessageRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemoryOutboxMessageRepository.java)
Added in-memory outbox persistence.
 - Supports pending batch reads and `findUnpublishedByIdForUpdate(...)` semantics for local locking behavior.

[InMemorySubmissionEvaluationJobRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/InMemorySubmissionEvaluationJobRepository.java)
Added in-memory evaluation job persistence.
 - Implements `claimForProcessing(...)` with status guard to prevent duplicate processing.

[EmailDailyQuotaUsageEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/EmailDailyQuotaUsageEntity.java)
Mapped daily quota table entity.
 - Stores `quotaDate`, `sentCount`, and `updatedAt` for persistent daily rate limiting.

[EmailDispatchJobEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/EmailDispatchJobEntity.java)
Mapped email dispatch job table entity.
 - Captures delivery status, attempts, next retry time, and failure metadata.

[JpaEmailDailyQuotaUsageRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaEmailDailyQuotaUsageRepository.java)
Added atomic quota reservation SQL.
 - `reserveExistingQuota(...)` uses conditional `UPDATE ... sent_count < :maxPerDay` to avoid racey read-then-write increments.

[JpaEmailDispatchJobRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaEmailDispatchJobRepository.java)
Added atomic email job claim SQL.
 - `claimForProcessing(...)` transitions only `QUEUED`/`RETRY_SCHEDULED` rows to `PROCESSING`.

[JpaOutboxMessageRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaOutboxMessageRepository.java)
Added pending query and row-level lock query.
 - `findByPublishedAtIsNullOrderByCreatedAtAsc(...)` drives scheduled polling.
 - `findByIdAndPublishedAtIsNull(...)` uses pessimistic write lock for single-row publish ownership.

[JpaPostgresEmailDailyQuotaUsageRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresEmailDailyQuotaUsageRepository.java)
Implemented robust Postgres quota reservation adapter.
 - `tryReserve(...)` uses atomic update first, insert fallback for missing day row, and conflict retry path.

[JpaPostgresEmailDispatchJobRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresEmailDispatchJobRepository.java)
Implemented Postgres adapter for email dispatch jobs.
 - Added `claimForProcessing(...)` bridge and mapping helpers `toEntity(...)` / `toDomain(...)`.

[JpaPostgresOutboxMessageRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresOutboxMessageRepository.java)
Implemented Postgres outbox adapter.
 - Added `findUnpublishedByIdForUpdate(...)` and pending paging read.
 - Keeps conversion logic in `toEntity(...)` / `toDomain(...)`.

[JpaPostgresSubmissionEvaluationJobRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresSubmissionEvaluationJobRepository.java)
Implemented Postgres adapter for evaluation jobs.
 - Added `claimForProcessing(...)` plus mapping methods for persistence/domain conversion.

[JpaSubmissionEvaluationJobRepository](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaSubmissionEvaluationJobRepository.java)
Added atomic evaluation job claim SQL.
 - `claimForProcessing(...)` increments attempts and switches to `PROCESSING` for eligible rows.

[OutboxMessageEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/OutboxMessageEntity.java)
Mapped outbox table entity.
 - Includes event metadata, payload JSON, publication timestamp, attempts, and last error.

[SubmissionEvaluationJobEntity](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/SubmissionEvaluationJobEntity.java)
Mapped submission evaluation job table entity.
 - Tracks lifecycle status, attempts, retry timing, and failure metadata.

[V3__add_rabbitmq_outbox_and_async_processing](backend/src/main/resources/db/migration/V3__add_rabbitmq_outbox_and_async_processing.sql)
Introduced schema for asynchronous messaging and evaluation lifecycle.
 - Created `outbox_messages`, `email_dispatch_jobs`, `email_daily_quota_usage`, and `submission_evaluation_jobs` tables.
 - Extended `submission_attempts` with status/evaluation/feedback columns and added supporting indexes.

[V4__allow_processing_email_dispatch_status](backend/src/main/resources/db/migration/V4__allow_processing_email_dispatch_status.sql)
Adjusted email job status constraint.
 - Replaced check constraint to include `PROCESSING` for idempotent claim-based consumer flow.

[RabbitMQ email and submission plan](plans/5-rabbitmq-email-and-submission-processing-plan.md)
Added full implementation and rollout plan with appendix.
 - Defines architecture, topology, outbox reliability pattern, retry/DLQ strategy, and operational guidance.
 - Includes iteration-by-iteration delivery plan and implementation-ready queue/property matrix.
