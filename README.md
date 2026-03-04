# LiveQuiz

## 🎯 Product Vision
LiveQuiz is a real-time feedback tool for classrooms. It allows instructors to launch quick quizzes during lectures to verify student comprehension immediately. The goal is to create an active learning loop between the teacher and students.

In the current domain model, `Lecture` is the aggregate root. Quiz interactions are modeled as lecture behavior:
- instructors create ordered questions and unlock them when needed,
- students advance through the oldest unlocked question they have not submitted yet,
- submissions keep full history while the latest attempt is canonical.

## 🛠 Technical Goals
This project serves as a comprehensive learning exercise for **Java Spring Boot** and **Angular**.

### Core Constraints
- **Manual Coding**: The user writes every line of code. The AI's role is to explain concepts and guide, not to do the work.
- **Deep Understanding**: No "magic" code. Every annotation, configuration, and pattern must be understood.
- **Architecture**: Strict adherence to **Domain-Driven Design (DDD)** and **Clean Architecture** principles.

## ✅ Implemented Capabilities
- JWT authentication with instructor/student roles
- Backend teacher registry for instructor role classification (feature-flagged rollout)
- Postgres-backed instructor email/password authentication with bcrypt hashes
- Instructor multi-lecture workflow (list, create, open by route, refresh-safe context)
- Lecture creation and ordered question management
- Question unlock flow (`unlock specific` and `unlock next`)
- Instructor analytics and per-question student answer history drilldown
- Instructor-generated invite flow (6-char code + QR-friendly token URL, max 24h)
- Student route-driven journey (lecture list, room re-entry, token deep-link join)
- Student return authentication via school-email magic link (`request-login` -> secure link -> JWT)
- Explicit invite error semantics (`INVITE_NOT_FOUND`, `INVITE_REVOKED`, `INVITE_EXPIRED`)
- Student progression API for "next pending unlocked question"
- Submission cooldown policy with `429` rejection and retry metadata

## 🔐 Student Return Access

- Students can request a return link from `/auth/login` using their `@ynov.com` email.
- Backend endpoint: `POST /api/auth/students/request-login` (generic non-enumerating response).
- Link consumption endpoint: `POST /api/auth/students/verify-email` returns a student JWT.
- For `REGISTERED_UNVERIFIED` students, the same link both verifies email and signs them in.

## 📂 Project Structure
This repository is a monorepo containing:
- `backend/`: Java Spring Boot application
- `frontend/`: Angular application

## 🧰 Prerequisites
- Java 17+
- Bun (preferred for frontend scripts) and Node.js runtime
- Docker Desktop (for Postgres and RabbitMQ local services)

## ⚡ Quick Start (local, in-memory backend)
Use this when you want to run fast without Postgres.

1. Start backend with in-memory profile:
   - `cd backend`
   - `SPRING_PROFILES_ACTIVE=in-memory ./mvnw spring-boot:run`
2. Install frontend dependencies:
   - `cd ../frontend`
   - `bun install`
3. Run frontend:
   - `bun run start`
4. Open app:
   - Frontend: `http://localhost:4200`
   - Backend health: `http://localhost:8080/api/health`

Default in-memory instructor login:
- Email: `instructor@ynov.com`
- Password: `password`

## 🐘 Full Local Setup (Postgres profile)
Use this when you want the default backend profile and Flyway migrations.

1. Start infra services:
   - `cd backend`
   - `docker compose up -d`
2. Set required JWT secret (required outside in-memory profiles):
   - `export LIVEQUIZ_JWT_SECRET='replace-with-a-long-random-secret'`
3. (Optional) Override DB/Rabbit config if needed:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_RABBITMQ_HOST`
   - `SPRING_RABBITMQ_PORT`
4. Run backend:
   - `./mvnw spring-boot:run`
5. Run frontend (separate terminal):
   - `cd ../frontend`
   - `bun install`
   - `bun run start`

## 🐳 Full Docker Stack (optional)

The root `docker-compose.yml` runs frontend, backend, Postgres, and RabbitMQ as containers.

1. Create a `.env.production` file at repository root with required values (DB, RabbitMQ, JWT secret, mail settings).
2. Start stack:
   - `docker compose up -d --build`
3. Access app:
   - Frontend: `http://localhost`

## 👩‍🏫 Teacher Account Setup

There are two layers involved in instructor access:

1. Authentication account (login credentials)
2. Teacher classification (who should receive `INSTRUCTOR` in JWT)

In `postgres` profile, instructor credentials come from DB table `instructor_accounts`.

### 1) Generate a bcrypt password hash (CLI)

From `backend/`:

- Interactive prompt (recommended): `./scripts/hash-password.sh`
- Stdin mode (safe for automation): `printf '%s\n' 'MyStrongPassword123!' | ./scripts/hash-password.sh`

The command prints a bcrypt hash you can copy into SQL.

### 2) Create or update instructor authentication account

```sql
INSERT INTO instructor_accounts (email, password_hash, active, created_at, updated_at)
VALUES ('teacher@ynov.com', '<PASTE_BCRYPT_HASH_HERE>', TRUE, NOW(), NOW())
ON CONFLICT (email)
DO UPDATE SET password_hash = EXCLUDED.password_hash, active = EXCLUDED.active, updated_at = NOW();
```

### Teacher role classification modes

- Default mode (`LIVEQUIZ_TEACHER_ROLE_CLASSIFICATION_ENABLED=false`):
  - Login role comes from legacy Spring authorities.
- Registry mode (`LIVEQUIZ_TEACHER_ROLE_CLASSIFICATION_ENABLED=true`):
  - Login role is resolved from `teacher_identities` table.
  - If a principal is missing or inactive in registry, role falls back to `STUDENT`.

### Enable registry mode

Set environment variable before starting backend:

- `export LIVEQUIZ_TEACHER_ROLE_CLASSIFICATION_ENABLED=true`

### 3) Add or activate teacher classification (for registry mode)

`principal_id` must match the instructor login email (normalized lowercase).

```sql
INSERT INTO teacher_identities (principal_id, active, created_at, updated_at)
VALUES ('teacher@ynov.com', TRUE, NOW(), NOW())
ON CONFLICT (principal_id)
DO UPDATE SET active = EXCLUDED.active, updated_at = NOW();
```

If Postgres runs in Docker from `backend/docker-compose.yml`, you can execute:

- `docker exec -it livequiz-db psql -U user -d livequiz`

Then run the SQL above.

### Remove teacher access

```sql
UPDATE teacher_identities
SET active = FALSE, updated_at = NOW()
WHERE principal_id = 'teacher@ynov.com';
```

Optional: disable login account too.

```sql
UPDATE instructor_accounts
SET active = FALSE, updated_at = NOW()
WHERE email = 'teacher@ynov.com';
```

## ✅ Common Commands

- Backend unit tests: `cd backend && ./mvnw test`
- Backend integration + verify: `cd backend && ./mvnw verify`
- Frontend tests: `cd frontend && bun run test`
- Frontend build: `cd frontend && bun run build`
