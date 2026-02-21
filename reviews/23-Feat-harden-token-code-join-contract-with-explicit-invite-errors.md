[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing token/code join contract hardening.
 - Marked `Iteration 15 - Token/code join contract hardening (explicit invite errors)` as complete.

[JoinLectureUseCase](backend/src/main/java/com/livequiz/backend/application/JoinLectureUseCase.java)
Refined join flow to resolve invite state explicitly and return stronger error contracts.
 - Replaced active-only lookup with latest-invite lookup per credential.
 - Added non-trivial state validation `ensureInviteUsable(...)` with explicit outcomes:
   - `INVITE_NOT_FOUND` for invalid credentials,
   - `INVITE_REVOKED` for revoked invites,
   - `INVITE_EXPIRED` for expired invites.
 - Preserved idempotent success for already-enrolled students even when invite later becomes non-active.

[LectureInviteRepository contract](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureInviteRepository.java)
Extended invite repository contract for explicit state inspection.
 - Added `findLatestByTokenHash(...)` and `findLatestByJoinCode(...)` to support post-resolution state checks.
 - Implemented in in-memory and JPA adapters (`InMemoryLectureInviteRepository`, `JpaPostgresLectureInviteRepository`, `JpaLectureInviteRepository`).

[StudentFlowIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java)
Expanded integration tests for invite-state error semantics and idempotent enrollment behavior.
 - Added revoked-invite join error contract test (`INVITE_REVOKED`).
 - Added expired-invite join error contract test (`INVITE_EXPIRED`).
 - Added idempotent-join-after-revocation test for previously enrolled student.
 - Added helper `expireInvite(...)` for deterministic expired-state setup in in-memory profile.
