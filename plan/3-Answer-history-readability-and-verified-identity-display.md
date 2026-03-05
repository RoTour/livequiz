## Context
In the instructor lecture workspace (`/instructor/lectures/:lectureId`), the answer history panel currently renders raw student UUID-like IDs and ISO timestamps. This slows scanning and makes recent activity hard to understand at a glance.

## Goal
Improve instructor readability of question answer history by:
- preferring verified student email when available
- formatting timestamps in a human-readable locale format
- applying clearer visual hierarchy and spacing in the history list

## Scope and Non-goals
In scope:
- Extend answer-history API response with optional verified email per student.
- Keep `studentId` as fallback identity when no verified email exists.
- Update frontend history rendering to use formatted dates and higher-clarity row layout.
- Add/adjust automated tests for backend and frontend behavior.

Non-goals:
- Changing enrollment or authentication flows.
- Exposing unverified student email addresses.
- Redesigning unrelated instructor panels.

## Design principles
- Preserve DDD/Clean Architecture boundaries:
  - Domain remains source of identity verification status.
  - Application orchestrates answer history projection.
  - Infrastructure adapts API DTOs without leaking persistence details.
  - Interface applies readability-focused presentation only.
- Keep changes reversible and incremental.
- Use recognition-over-recall UX: readable identity label, chunked metadata, and consistent spacing rhythm.

## Iterations
- [I1] Add verified-email projection to answer history API
  - Type: backend
  - Why: The UI cannot prefer verified email until API exposes it explicitly.
  - Domain: Reuse `StudentIdentity` invariants (`REGISTERED_VERIFIED` only) without changing domain rules.
  - Application: Extend `GetQuestionStudentAnswerHistoryUseCase` response model to include optional `studentEmail` resolved from `StudentIdentityRepository` only for verified identities.
  - Infrastructure: Extend `LectureController.StudentAnswerHistoryResponse` DTO mapping with `studentEmail`.
  - Interface: N/A.
  - Risks/open questions:
    - Additional repository lookup per enrolled student (acceptable at current scope; can batch later if needed).
  - Acceptance criteria:
    - API response contains `studentEmail` when student identity is verified.
    - API response contains `studentEmail = null` for anonymous/unverified students.
    - Existing `studentId`, `latestAnswerAt`, `attemptCount`, and `latestAnswerText` behavior remains unchanged.

- [I2] Improve instructor answer-history readability and spacing
  - Type: frontend
  - Why: Instructors need fast visual parsing for identity, recency, attempts, and latest answer text.
  - Domain: N/A.
  - Application: Consume extended `StudentAnswerHistoryResponse` contract in workspace flow.
  - Infrastructure: N/A.
  - Interface:
    - Show `studentEmail ?? studentId` as primary identifier.
    - Format `latestAnswerAt` using existing `HumanDatePipe`.
    - Replace dense inline row with structured history item blocks and spacing tuned for readability.
  - Risks/open questions:
    - Long answer text wrapping; mitigate with constrained line-height and preserved whitespace.
  - Acceptance criteria:
    - History list no longer shows raw ISO dates.
    - Verified-email students show email label instead of UUID/ID.
    - Layout is readable on desktop and mobile (stacked metadata and breathing room).
