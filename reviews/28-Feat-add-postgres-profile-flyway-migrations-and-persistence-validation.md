[README](README.md)
Documented the new backend persistence model and default runtime behavior.
 - Added a dedicated section for backend profiles, clarifying `postgres` as default and `in-memory`/`memory` as lightweight alternatives.
 - Added explicit Postgres startup instructions and environment variables (`LIVEQUIZ_JWT_SECRET`, datasource overrides).

[ROADMAP](ROADMAP.md)
Marked persistence hardening milestones as completed.
 - Updated Phase 5 checklist items to reflect completed Postgres profile wiring and migration tooling.
 - Kept remaining hardening items visible (`managed identity provider`, dashboards, audit/rate limiting).

[backend/docker-compose.yml](backend/docker-compose.yml)
Fixed Postgres volume mount to persist actual database files.
 - Changed mount target to `/var/lib/postgresql/data`, which is the canonical Postgres data directory path.

[backend/pom.xml](backend/pom.xml)
Extended backend dependencies for Postgres integration testing and migrations.
 - Added Testcontainers modules (`org.testcontainers:junit-jupiter`, `org.testcontainers:postgresql`) for containerized persistence integration tests.
 - Added Flyway runtime dependencies (`spring-boot-starter-flyway`, `org.flywaydb:flyway-database-postgresql`) so migration execution supports PostgreSQL.

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaLectureRepository.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaLectureRepository.java)
Expanded the repository contract with explicit read-model queries for lecture aggregates.
 - Added native query method `findQuestionRowsByLectureId(...)` returning projection interface `LectureQuestionRow` to fetch ordered question rows directly from `lecture_questions`.
 - Added native query method `findUnlockedQuestionIdsByLectureId(...)` to load unlocked question ids from `lecture_unlocked_questions`.
 - Kept `findById(...)` and `findByCreatedByInstructorIdOrderByCreatedAtDesc(...)` as primary aggregate lookup APIs.

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureRepository.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/JpaPostgresLectureRepository.java)
Refined aggregate mapping logic for Postgres-backed lecture reads.
 - `save(...)` now converts unlocked ids to `LinkedHashSet` before persisting `LectureEntity`, preserving deterministic order semantics.
 - `findById(...)` and `findByCreatedByInstructorId(...)` are marked `@Transactional(readOnly = true)` and now compose aggregate state from base entity + explicit query methods.
 - Non-trivial method `toDomain(...)` now maps from `LectureQuestionRow` projection + unlocked id list into domain `Lecture` and `Question` objects.

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEnrollmentEntity.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEnrollmentEntity.java)
Aligned JPA mapping with migration schema constraints.
 - Added `@Basic(optional = false)` and `@Column(name = "enrolled_at", nullable = false)` for `enrolledAt` so Hibernate validation matches DDL (`TIMESTAMPTZ NOT NULL`).

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEntity.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureEntity.java)
Updated lecture entity field metadata and collection shapes to match SQL schema and domain expectations.
 - Added column constraints (`title` length/nonnull, `createdByInstructorId` length) to match migration-defined boundaries.
 - Switched `unlockedQuestionIds` from `List<String>` to `Set<String>` (backed by `LinkedHashSet`) to model uniqueness and stable order.
 - Added eager element collection mapping metadata for `questions` and `unlockedQuestionIds`, and updated constructors/getters to use `Set<String>`.

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureInviteEntity.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureInviteEntity.java)
Added explicit column-level schema constraints for invite persistence.
 - Applied length/nullable annotations for `id`, `lectureId`, `createdByInstructorId`, `joinCode`, `tokenHash`, `createdAt`, and `expiresAt` to match migration DDL.
 - Kept `revokedAt` nullable to support active invite lifecycle.

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureQuestionEmbeddable.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/LectureQuestionEmbeddable.java)
Hardened embedded question mapping against strict schema validation.
 - Added explicit `@Column` constraints for identifiers and numeric fields.
 - Mapped text-heavy columns (`prompt`, `modelAnswer`) with `columnDefinition = "text"` and non-null constraints to align with migration table design.

[backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/SubmissionEntity.java](backend/src/main/java/com/livequiz/backend/infrastructure/persistence/jpa/SubmissionEntity.java)
Applied explicit mapping constraints for submission attempt persistence.
 - Added length/non-null constraints on id and foreign-key-like fields (`lectureId`, `questionId`, `studentId`).
 - Mapped `answerText` to SQL `TEXT` and marked timestamps as non-null to match DDL and validation.

[backend/src/main/resources/application-in-memory.properties](backend/src/main/resources/application-in-memory.properties)
Disabled Flyway for in-memory profile.
 - Added `spring.flyway.enabled=false` to prevent migration startup paths in non-datasource profile.

[backend/src/main/resources/application-memory.properties](backend/src/main/resources/application-memory.properties)
Disabled Flyway for `memory` alias profile.
 - Added `spring.flyway.enabled=false` to mirror `in-memory` behavior and avoid accidental datasource wiring.

[backend/src/main/resources/application.properties](backend/src/main/resources/application.properties)
Simplified shared defaults and moved DB specifics into profile-scoped config.
 - Removed inline datasource/JPA settings from base properties.
 - Kept `spring.profiles.active=postgres` so runtime defaults to persistent profile while preserving shared app/security/domain config.

[backend/README.md](backend/README.md)
Introduced backend-specific runtime profile guide.
 - Added profile override patterns (`SPRING_PROFILES_ACTIVE`, Maven profile flag, generic arguments, jar args).
 - Documented available profiles and operational notes for JWT secret and datasource overrides.

[backend/src/main/resources/application-postgres.properties](backend/src/main/resources/application-postgres.properties)
Added dedicated Postgres profile configuration.
 - Declares datasource settings with env-var overrides and local defaults.
 - Sets `spring.jpa.hibernate.ddl-auto=validate` and enables Flyway migration locations/baseline behavior for schema-managed startup.

[backend/src/main/resources/db/migration/V1__init_livequiz_schema.sql](backend/src/main/resources/db/migration/V1__init_livequiz_schema.sql)
Created initial Flyway migration for all persistence tables and indexes.
 - Defines core tables: `lectures`, `lecture_questions`, `lecture_unlocked_questions`, `lecture_invites`, `lecture_enrollments`, `submission_attempts`.
 - Adds foreign keys, uniqueness constraints, and check constraints to enforce domain invariants at DB layer.
 - Adds targeted indexes for ownership listing, invite resolution, enrollment lookup, and submission analytics query patterns.

[backend/src/test/java/com/livequiz/backend/infrastructure/persistence/PostgresPersistenceIT.java](backend/src/test/java/com/livequiz/backend/infrastructure/persistence/PostgresPersistenceIT.java)
Added end-to-end persistence integration test against real Postgres container.
 - Class `PostgresPersistenceIT` uses `@SpringBootTest`, `@ActiveProfiles("postgres")`, and Testcontainers `PostgreSQLContainer` to validate production-like wiring.
 - Non-trivial method `configureProperties(...)` injects container datasource values through `DynamicPropertyRegistry`.
 - Test `should_write_lecture_invite_enrollment_and_submission_to_postgres()` verifies write paths across all repositories.
 - Test `should_read_the_same_data_after_context_restart()` validates read/query behavior after Spring context restart, including aggregate reads, invite lookups, enrollment queries, and submission analytics projections.
