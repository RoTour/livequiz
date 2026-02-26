# 5 - RabbitMQ Email Delivery and Submission Processing Plan

## Context

Current behavior is synchronous for both workloads:

- Student verification emails are sent directly from request-path use cases.
- Student submissions are persisted quickly, but no asynchronous evaluation pipeline exists yet.

We now need a queue-driven design for two production requirements:

1. Email sending must tolerate failures and respect SES limits (`1 email/second`, `200/day`).
2. Submission evaluation must move to a long-running asynchronous worker flow (LLM-ready later).

RabbitMQ runtime setup is handled in Docker Compose; this plan focuses on backend integration and delivery slices.

## Goals

- Integrate RabbitMQ with durable queues and dead-letter handling.
- Queue all outbound student emails and support safe retries.
- Enforce SES sending limits without dropping queued emails.
- Process student answer evaluations asynchronously after submission.
- Keep existing lecture progression and submission contracts backward compatible.
- Add observability and operational controls for queue lag, retries, and failures.

## Scope and Non-Goals

In scope:

- Backend domain/application/infrastructure changes for queue publish/consume.
- Flyway migrations for outbox, delivery jobs, and evaluation tracking.
- Retry, idempotency, and DLQ policy for both workflows.
- API/read-model adjustments needed to surface async evaluation status.
- Automated tests for queue-driven behavior.

Out of scope:

- LLM prompt quality, rubric design, and model selection.
- Frontend redesign beyond status/read-model wiring.
- SES account limit increase requests.

## Design Principles

- Preserve DDD + clean architecture boundaries.
- Use at-least-once message delivery with idempotent consumers.
- Avoid dual-write risks with transactional outbox publishing.
- Keep message payloads versioned and minimal (IDs + metadata).
- Treat RabbitMQ outages as degradations, not data-loss events.

## Target Messaging Topology

Single durable topic exchange:

- `livequiz.events`

Email routing:

- Routing key: `email.dispatch.requested.v1`
- Main queue: `livequiz.email.dispatch.v1`
- Retry queue: `livequiz.email.dispatch.retry.v1` (TTL + DLX back to main)
- Dead-letter queue: `livequiz.email.dispatch.dlq.v1`

Submission evaluation routing:

- Routing key: `submission.evaluate.requested.v1`
- Main queue: `livequiz.submission.evaluate.v1`
- Retry queue: `livequiz.submission.evaluate.retry.v1` (TTL + DLX back to main)
- Dead-letter queue: `livequiz.submission.evaluate.dlq.v1`

Notes:

- Keep queues durable and messages persistent.
- Use standard TTL/DLX retry flow (no delayed-message plugin dependency).

## Shared Reliability Pattern: Transactional Outbox

To prevent DB-write/publish mismatch:

- Add `outbox_messages` table.
- In the same DB transaction as business writes, persist an outbox row.
- A publisher component reads pending rows, publishes to RabbitMQ with confirm, then marks row as published.
- On publish failure, keep row pending for retry.

Minimal outbox fields:

- `id`, `eventType`, `routingKey`, `payloadJson`, `createdAt`, `publishedAt`, `attemptCount`, `lastError`.

## Use Case A: Email Sending via Queue (SES-Constrained)

### Producer integration

Trigger points (initial slice):

- `RegisterStudentEmailUseCase`
- `ResendStudentVerificationUseCase`

Producer behavior:

- Build `EmailDispatchRequestedV1` message.
- Store email dispatch job + outbox event in one transaction.
- Return API response immediately (non-blocking delivery).

Message contract (v1):

- `messageId` (UUID)
- `templateType` (e.g. `STUDENT_EMAIL_VERIFICATION`)
- `toEmail`
- `templateVariables` (JSON object)
- `requestedAt`
- `correlationId`

### Consumer integration

Worker behavior:

- Consume one message at a time (`concurrency=1`) for predictable quota control.
- Enforce `1 email/second` rate limit before SES call.
- Enforce `200/day` using persisted quota counters (`email_daily_quota_usage`).
- Send through provider adapter (`SesStudentVerificationEmailSender` or equivalent).
- Record delivery result (`SENT`, `RETRY_SCHEDULED`, `FAILED_FINAL`).

Quota policy details:

- Per-second limit: application rate limiter + single consumer concurrency.
- Daily limit: reserve quota slot transactionally before provider call.
- If daily quota exhausted: republish with delay until next quota window (via retry queue message TTL).

Retry policy:

- Retryable errors: network timeout, 5xx provider errors, throttling.
- Non-retryable errors: invalid recipient format, rejected template payload.
- Exponential backoff by attempt count (example: `30s`, `2m`, `10m`, `30m`, `2h`).
- After max attempts, route to DLQ and persist failure reason.

## Use Case B: Submission Evaluation via Queue (Long-Running)

### Producer integration

On `SubmitAnswerUseCase` success:

- Keep current validations and persistence path.
- Persist submission with public status `AWAITING_EVALUATION`.
- Create evaluation job record + outbox event in same transaction.
- Return submit response immediately (no evaluation latency in request path).

Message contract (v1):

- `messageId` (UUID)
- `submissionId`
- `lectureId`
- `questionId`
- `studentId`
- `requestedAt`
- `correlationId`

### Consumer integration

Worker behavior:

- Idempotently lock/mark evaluation job as `PROCESSING`.
- Load submission/question context from repositories.
- Call `AnswerEvaluationProvider` application port.
- Persist evaluation result (`CORRECT`, `INCORRECT`, `INCOMPLETE`) and feedback payload.
- Mark job completed, or schedule retry/DLQ on failure.

LLM-ready abstraction:

- Introduce interface (application port): `AnswerEvaluationProvider`.
- First implementation can be deterministic stub/rules-based for tests.
- Later swap to LLM adapter without changing use case/consumer contracts.

Failure handling:

- Transient evaluator/provider failures -> retry queue with backoff.
- Max-attempt exhaustion -> DLQ + `FAILED_FINAL` processing state.
- Public answer status remains stable and explicit (`AWAITING_EVALUATION` until resolved).

## Data Model and Migration Plan

Add tables:

- `outbox_messages`
- `email_dispatch_jobs`
- `email_daily_quota_usage`
- `submission_evaluation_jobs`

Extend `submission_attempts`:

- `answer_status` (`AWAITING_EVALUATION`, `CORRECT`, `INCORRECT`, `INCOMPLETE`)
- `evaluation_completed_at`
- feedback columns (or JSON) for evaluator output

Indexes:

- Outbox pending lookup (`published_at is null`, `created_at`)
- Email job state + next-attempt lookup
- Evaluation job state + next-attempt lookup
- Submission lookup by lecture/question/student (already used in progression)

## Security and Privacy Considerations

- Do not trust message payload identity beyond repository re-checks.
- Keep queue payloads minimal; avoid embedding JWTs/secrets.
- Use correlation IDs for audit trails.
- Keep DLQ payload access restricted to operators.

## Observability and Operations

Metrics:

- Queue depth, consumer lag, processing duration.
- Retry count, DLQ count, send success/failure rates.
- Daily quota utilization and next reset timestamp.

Logging:

- Structured logs for `messageId`, `correlationId`, `routingKey`, `attemptCount`.
- Explicit error classification (`RETRYABLE`, `NON_RETRYABLE`).

Runbook actions:

- Replay DLQ message after root-cause fix.
- Pause/resume consumers during incidents.
- Inspect quota table when emails stall due to daily cap.

## Iteration Plan

### Iteration 01 - Baseline Contract Lock

Goal: prevent regressions before queue integration.

Scope:

- Strengthen current tests around student email auth and submission flow.
- Capture current API response contracts for submit and verification endpoints.

Acceptance:

- Existing behavior remains green before introducing async internals.

### Iteration 02 - RabbitMQ Infrastructure Wiring

Goal: establish messaging infrastructure in backend.

Scope:

- Add Spring AMQP configuration for exchange/queues/bindings.
- Add health indicator/config properties for RabbitMQ connectivity.
- Add profile-safe defaults for local/dev.

Acceptance:

- App starts with RabbitMQ config and declares required topology.

### Iteration 03 - Transactional Outbox Foundation

Goal: ensure reliable message publication.

Scope:

- Add outbox table + repository + scheduled publisher.
- Add publish confirmation handling and retry-on-failure loop.

Acceptance:

- Business writes can persist outbox events even if RabbitMQ is temporarily unavailable.

### Iteration 04 - Email Producer Path

Goal: enqueue student verification emails from auth use cases.

Scope:

- Update register/resend use cases to create email dispatch jobs and outbox events.
- Keep API responses unchanged and non-blocking.

Acceptance:

- Email requests are queued; request path no longer sends directly.

### Iteration 05 - Email Consumer with SES Quota Enforcement

Goal: send queued emails safely under provider limits.

Scope:

- Build email dispatch consumer.
- Implement per-second and daily quota controls.
- Persist send results and attempts.

Acceptance:

- Consumer respects `1/sec` and `200/day` while eventually delivering queued messages.

### Iteration 06 - Email Retry, DLQ, and Replay Procedure

Goal: harden failure recovery.

Scope:

- Implement retry classification and backoff.
- Route exhausted failures to DLQ.
- Document/operator command path to replay dead letters.

Acceptance:

- Retryable failures recover automatically; terminal failures are diagnosable and replayable.

### Iteration 07 - Submission Producer Path

Goal: publish evaluation requests after answer submission.

Scope:

- Extend submission persistence model with answer status and evaluation job linkage.
- Emit submission evaluation event through outbox in submit flow.

Acceptance:

- Submit endpoint remains fast and returns `AWAITING_EVALUATION` while evaluation is queued.

### Iteration 08 - Submission Evaluation Worker + Provider Port

Goal: process queued evaluations asynchronously.

Scope:

- Add evaluation consumer and `AnswerEvaluationProvider` port.
- Implement deterministic provider adapter for initial behavior/testing.
- Persist feedback + final status updates.

Acceptance:

- Queued submissions transition from `AWAITING_EVALUATION` to evaluated states.

### Iteration 09 - Retry/DLQ for Submission Evaluations

Goal: protect long-running processing reliability.

Scope:

- Add transient/permanent error classification for evaluator failures.
- Add retry queue/backoff and DLQ routing for evaluation messages.
- Track processing state (`QUEUED`, `PROCESSING`, `RETRY_SCHEDULED`, `FAILED_FINAL`, `COMPLETED`).

Acceptance:

- Evaluations recover from temporary failures and expose terminal failures for operations.

### Iteration 10 - End-to-End Validation and Documentation

Goal: close the phase with reliable operations.

Scope:

- Add integration tests for outbox -> queue -> consumer -> persistence flow.
- Validate email quota behavior and retry/backoff.
- Validate submission status transitions and idempotent duplicate handling.
- Update `README.md`/`ROADMAP.md` and operational notes.

Acceptance:

- Queue-driven email and submission workflows are test-covered, observable, and documented.

## Suggested Validation Commands

- Backend targeted tests: `./mvnw -Dtest=StudentEmailAuthIT test`
- Submission flow IT: `./mvnw -Dit.test=StudentFlowIT failsafe:integration-test failsafe:verify`
- Full backend validation before merge: `./mvnw verify`

## Definition of Done

- Email delivery is queue-driven with enforced SES rate/daily limits.
- Email retries and DLQ handling are implemented and operationally documented.
- Student submissions are evaluated asynchronously through RabbitMQ consumers.
- Evaluation worker is LLM-ready via provider abstraction.
- Outbox pattern prevents message loss across DB/publish boundaries.
- Integration tests cover happy paths and key failure/retry scenarios.

## Appendix A - Queue and Consumer Configuration Matrix

This appendix is implementation-ready naming and runtime guidance for Spring AMQP wiring.

### A1. Broker object matrix

Shared exchange:

- Exchange: `livequiz.events`
- Type: `topic`
- Durable: `true`

Bindings and queues:

| Purpose | Routing key | Queue | Queue args |
| --- | --- | --- | --- |
| Email main | `email.dispatch.requested.v1` | `livequiz.email.dispatch.v1` | `x-dead-letter-exchange=livequiz.events`, `x-dead-letter-routing-key=email.dispatch.failed.v1` |
| Email retry | `email.dispatch.retry.v1` | `livequiz.email.dispatch.retry.v1` | `x-dead-letter-exchange=livequiz.events`, `x-dead-letter-routing-key=email.dispatch.requested.v1` |
| Email DLQ | `email.dispatch.failed.v1` | `livequiz.email.dispatch.dlq.v1` | none |
| Submission eval main | `submission.evaluate.requested.v1` | `livequiz.submission.evaluate.v1` | `x-dead-letter-exchange=livequiz.events`, `x-dead-letter-routing-key=submission.evaluate.failed.v1` |
| Submission eval retry | `submission.evaluate.retry.v1` | `livequiz.submission.evaluate.retry.v1` | `x-dead-letter-exchange=livequiz.events`, `x-dead-letter-routing-key=submission.evaluate.requested.v1` |
| Submission eval DLQ | `submission.evaluate.failed.v1` | `livequiz.submission.evaluate.dlq.v1` | none |

Notes:

- Retry delay is set per message using `expiration` to support exponential backoff.
- Keep all queues durable and all published messages persistent.

### A2. Message envelope contract (all events)

Required JSON fields:

- `messageId` (UUID, globally unique)
- `eventType` (`EmailDispatchRequestedV1`, `SubmissionEvaluationRequestedV1`)
- `schemaVersion` (`v1`)
- `correlationId` (trace across API -> outbox -> worker)
- `causationId` (upstream command/request id when available)
- `occurredAt` (ISO-8601 UTC)
- `payload` (event-specific body)

AMQP properties:

- `contentType=application/json`
- `deliveryMode=PERSISTENT`
- `messageId=<same as envelope messageId>`
- Header `x-attempt=<int>` (starts at `1`)

### A3. Consumer runtime defaults

| Consumer | Queue | Concurrency | Prefetch | Ack mode | Idempotency key |
| --- | --- | --- | --- | --- | --- |
| Email dispatch worker | `livequiz.email.dispatch.v1` | `1` | `1` | `MANUAL` | `messageId` + unique `email_dispatch_jobs.message_id` |
| Submission evaluation worker | `livequiz.submission.evaluate.v1` | `2` (start), scale to `4` | `1` | `MANUAL` | `submissionId` + unique `submission_evaluation_jobs.submission_id` |

Ack/nack policy:

- Success: `ack`.
- Retryable failure: publish to retry routing key with incremented `x-attempt` + delay, then `ack` original.
- Non-retryable or max-attempt reached: reject to DLQ path (or publish failed event then `ack`, one strategy only).

### A4. Retry/backoff schedule

Email dispatch backoff:

| Attempt | Delay | Action after delay |
| --- | --- | --- |
| 1 | `30s` | re-enter `email.dispatch.requested.v1` |
| 2 | `2m` | re-enter `email.dispatch.requested.v1` |
| 3 | `10m` | re-enter `email.dispatch.requested.v1` |
| 4 | `30m` | re-enter `email.dispatch.requested.v1` |
| 5 | `2h` | re-enter `email.dispatch.requested.v1` |
| 6+ | none | route to `email.dispatch.failed.v1` |

Submission evaluation backoff:

| Attempt | Delay | Action after delay |
| --- | --- | --- |
| 1 | `15s` | re-enter `submission.evaluate.requested.v1` |
| 2 | `1m` | re-enter `submission.evaluate.requested.v1` |
| 3 | `5m` | re-enter `submission.evaluate.requested.v1` |
| 4 | `15m` | re-enter `submission.evaluate.requested.v1` |
| 5 | `1h` | re-enter `submission.evaluate.requested.v1` |
| 6+ | none | route to `submission.evaluate.failed.v1` |

### A5. SES quota enforcement defaults

- Per-second send cap: worker-level limiter `1 permit/second`.
- Daily send cap: `200` from persisted quota counter table.
- Daily reset: UTC midnight via quota window key (`YYYY-MM-DD`).
- If daily cap reached: schedule retry for next day window start + jitter (`30-120s`).

### A6. Outbox publisher defaults

Publisher job defaults:

- Poll interval: every `500ms`.
- Batch size: `100` rows ordered by `created_at`.
- Publish confirm timeout: `5s`.
- Max immediate retries per row before leaving pending: `3` (then next scheduler cycle retries).

Outbox query contract:

- Select pending where `published_at is null` ordered by `created_at asc`.
- Use optimistic lock/version column or `for update skip locked` strategy in Postgres profile.

### A7. Proposed Spring property keys

```properties
livequiz.messaging.exchange=livequiz.events

livequiz.messaging.email.main-queue=livequiz.email.dispatch.v1
livequiz.messaging.email.retry-queue=livequiz.email.dispatch.retry.v1
livequiz.messaging.email.dlq=livequiz.email.dispatch.dlq.v1
livequiz.messaging.email.requested-routing-key=email.dispatch.requested.v1
livequiz.messaging.email.retry-routing-key=email.dispatch.retry.v1
livequiz.messaging.email.failed-routing-key=email.dispatch.failed.v1
livequiz.messaging.email.consumer-concurrency=1
livequiz.messaging.email.prefetch=1

livequiz.messaging.submission.main-queue=livequiz.submission.evaluate.v1
livequiz.messaging.submission.retry-queue=livequiz.submission.evaluate.retry.v1
livequiz.messaging.submission.dlq=livequiz.submission.evaluate.dlq.v1
livequiz.messaging.submission.requested-routing-key=submission.evaluate.requested.v1
livequiz.messaging.submission.retry-routing-key=submission.evaluate.retry.v1
livequiz.messaging.submission.failed-routing-key=submission.evaluate.failed.v1
livequiz.messaging.submission.consumer-concurrency=2
livequiz.messaging.submission.prefetch=1

livequiz.messaging.outbox.poll-interval-ms=500
livequiz.messaging.outbox.batch-size=100
livequiz.messaging.outbox.publish-confirm-timeout-ms=5000

livequiz.email.ses.max-per-second=1
livequiz.email.ses.max-per-day=200
```

### A8. Implementation acceptance checks

- Start stack and verify queue/exchange declarations exist and are durable.
- Submit one verification-email request and confirm: outbox row -> main queue message -> delivery job transition.
- Force transient email failure and verify retry queue TTL return flow.
- Submit one student answer and confirm asynchronous evaluation status transition.
- Force evaluator failure and verify retry then DLQ after max attempts.
