# 33 - Feat teacher role classification

## Why

Student authentication is now more dynamic, so instructor privileges must come from backend-owned business data instead of only login authorities.

## What changed

- Added teacher identity domain and repository boundary:
  - `backend/src/main/java/com/livequiz/backend/domain/teacher/TeacherIdentity.java`
  - `backend/src/main/java/com/livequiz/backend/domain/teacher/TeacherIdentityRepository.java`
- Added role resolution use case:
  - `backend/src/main/java/com/livequiz/backend/application/ResolveUserRoleUseCase.java`
- Added persistence adapters for teacher registry:
  - in-memory repository and seeder,
  - postgres JPA entity/repository/adapter,
  - migration `V5__add_teacher_identity_registry.sql`.
- Updated login role issuance in `JwtController`:
  - feature-flagged teacher-registry resolution,
  - safe fallback to legacy authority-based role extraction,
  - structured audit logging for role source and result.
- Added test user `instructor-candidate` to verify teacher-registry behavior versus legacy fallback.
- Added unit and integration coverage for:
  - registry-driven role classification,
  - default least-privilege fallback to `STUDENT`,
  - rollout toggle behavior.

## Rollout note

- `LIVEQUIZ_TEACHER_ROLE_CLASSIFICATION_ENABLED` defaults to `false` for safe deployment.
- Enable it to activate registry-based instructor classification.
