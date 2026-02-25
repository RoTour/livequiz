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
- Instructor multi-lecture workflow (list, create, open by route, refresh-safe context)
- Lecture creation and ordered question management
- Question unlock flow (`unlock specific` and `unlock next`)
- Instructor analytics and per-question student answer history drilldown
- Instructor-generated invite flow (6-char code + QR-friendly token URL, max 24h)
- Student route-driven journey (lecture list, room re-entry, token deep-link join)
- Explicit invite error semantics (`INVITE_NOT_FOUND`, `INVITE_REVOKED`, `INVITE_EXPIRED`)
- Student progression API for "next pending unlocked question"
- Submission cooldown policy with `429` rejection and retry metadata

## 📂 Project Structure
This repository is a monorepo containing:
- `backend/`: Java Spring Boot application
- `frontend/`: Angular application

## 💾 Backend Persistence Profiles
- Default runtime profile is `postgres`.
- In-memory profiles (`in-memory`, `memory`) remain available for fast local and test flows without a database.
- Postgres schema is managed by Flyway migrations under `backend/src/main/resources/db/migration/`.

## 🐘 Run Backend with Postgres
1. Start Postgres:
   - `cd backend && docker compose up -d`
2. Set JWT secret (required outside in-memory profiles):
   - `export LIVEQUIZ_JWT_SECRET='replace-with-a-long-random-secret'`
3. Run backend (uses `postgres` profile by default):
   - `cd backend && ./mvnw spring-boot:run`

Flyway runs automatically on startup. To override DB connection, set:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
