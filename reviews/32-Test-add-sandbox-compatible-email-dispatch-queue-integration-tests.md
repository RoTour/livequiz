[EmailDispatchQueueTestConfig](backend/src/test/java/com/livequiz/backend/infrastructure/messaging/EmailDispatchQueueTestConfig.java)
Introduced a dedicated integration-test wiring layer for queue and SES sandbox scenarios.
 - Added `@TestConfiguration` with primary beans to replace production integrations inside Spring Boot tests.
 - Implemented `FakeRabbitTemplate` (extends `RabbitTemplate`) and overrode `convertAndSend(...)` to capture exchange, routing key, payload, and AMQP headers for assertions.
 - Implemented `FakeSesStudentVerificationEmailSender` (implements `StudentVerificationEmailSender`) to collect dispatched email attempts without calling SES.
 - Added a local `ObjectMapper` bean to satisfy `RabbitStudentVerificationEmailDispatchService` construction in test context.
 - Added utility records/classes (`CapturedRabbitMessage`, `SentEmail`) plus helper methods (`messagesForRoutingKey`, `clear`, `payloadAsString`) to make assertions explicit and readable.

[EmailDispatchQueueSandboxCompatibilityIT](backend/src/test/java/com/livequiz/backend/infrastructure/messaging/EmailDispatchQueueSandboxCompatibilityIT.java)
Added an integration test validating the normal queue behavior under SES sandbox-like throughput constraints.
 - Bootstraps with `livequiz.messaging.enabled=true` and `livequiz.email.ses.max-per-second=1` in `in-memory` profile.
 - Uses `StudentVerificationEmailDispatchService.dispatch(...)` to enqueue verification requests, then `OutboxPublisher.publishPending()` to move outbox events to messaging.
 - Explicitly drives consumption through `EmailDispatchConsumer.consume(...)` and verifies each `EmailDispatchJob` is persisted as `SENT`.
 - Verifies there is no retry publication on the retry routing key and that outbox pending messages are drained (`OutboxMessageRepository.findPending(...)`).
 - Non-trivial classes/methods involved: `OutboxPublisher.publishPending`, `EmailDispatchConsumer.consume`, `EmailDispatchJobRepository.findByMessageId`.

[EmailDispatchQueueDailyQuotaIT](backend/src/test/java/com/livequiz/backend/infrastructure/messaging/EmailDispatchQueueDailyQuotaIT.java)
Added an integration test validating compatibility with SES daily sandbox quota exhaustion behavior.
 - Bootstraps with `livequiz.email.ses.max-per-day=1` so a second email in the same day must be deferred.
 - Dispatches two verification jobs, publishes outbox messages, and consumes both queue payloads.
 - Uses order-agnostic assertions over persisted jobs to ensure exactly one `SENT` and one `RETRY_SCHEDULED` status.
 - Verifies deferred job details: `attemptCount == 0`, `lastError == "SES_DAILY_QUOTA_EXHAUSTED"`, and non-null `nextAttemptAt`.
 - Verifies retry queue contract by asserting one retry message exists and carries `x-attempt=2` plus TTL/expiration metadata.
 - Non-trivial classes/methods involved: `EmailDispatchConsumer.consume`, `EmailDispatchJobRepository.findByMessageId`, grouping assertions over `EmailDispatchStatus`.

Generated commit message:
`test: add sandbox-compatible integration tests for email dispatch queue`
