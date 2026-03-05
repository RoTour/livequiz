## Context
The current submission pipeline auto-applies evaluation outcomes (`AWAITING_EVALUATION`, `CORRECT`, `INCORRECT`, `INCOMPLETE`) and exposes the latest submission status to students. Instructors can inspect answer history, but cannot perform explicit per-attempt reviews, control visibility, or accept/reject LLM-generated review suggestions before students see them.

The requested behavior is:
- instructors review each answer attempt manually,
- reviews can be draft or published,
- students only see published review outcomes,
- student fallback status is `AWAITING_REVIEW`,
- LLM review generation is integrated via OpenRouter but must be accepted by instructor before becoming student-visible,
- the negative status wording changes from `INCORRECT` to `NEEDS_IMPROVEMENT`.

## Goal
Deliver a teacher-first review workflow where each submission attempt can be reviewed and optionally published, and where LLM-generated reviews are advisory drafts requiring instructor acceptance before publication, while preserving existing lecture/question/submission boundaries and backwards-compatible operational behavior.

## Scope and Non-goals
In scope:
- Add manual instructor review commands for each submission attempt.
- Add published/unpublished visibility model.
- Add review outcome vocabulary: `CORRECT`, `INCOMPLETE`, `NEEDS_IMPROVEMENT`, and `AWAITING_REVIEW` fallback.
- Integrate OpenRouter-backed evaluator as a provider option for draft suggestions.
- Add explicit instructor acceptance flow for LLM suggestions.
- Update instructor and student read models/UI to reflect new workflow.

Non-goals:
- No automatic student publication of LLM output.
- No rubric builder or configurable scoring framework.
- No bulk moderation UI/actions.
- No notification system (email/push) for review publication.
- No forced migration of historical UI beyond safe status mapping.

## Design principles
- Keep submission as the consistency boundary for review state transitions.
- Separate review command intent (manual review, accept LLM draft, publish toggle) from read models.
- Preserve Clean Architecture dependency direction:
  - Domain: submission review invariants and transitions.
  - Application: use-case orchestration, authorization and ownership checks.
  - Infrastructure: persistence mapping, OpenRouter adapter, web endpoints.
  - Interface: instructor/student UI projections.
- Prefer append-safe and reversible schema changes with explicit migration mapping for legacy statuses.
- Keep every iteration deployable with deterministic fallback when OpenRouter is disabled.

## Iterations
- [I1 - Manual review core model and instructor command API]
  - Type: backend
  - Why: establish explicit instructor-owned review lifecycle per submission attempt before introducing LLM acceptance complexity.
  - Domain:
    - Introduce review outcome vocabulary (`CORRECT`, `INCOMPLETE`, `NEEDS_IMPROVEMENT`).
    - Add submission behaviors for manual review create/update and publish/unpublish.
    - Preserve invariant: review outcome must be valid; reviewer identity required for write actions.
  - Application:
    - Add use case to upsert manual review for a submission attempt with publish flag.
    - Enforce instructor ownership of lecture/question/submission relation.
  - Infrastructure:
    - Extend persistence schema/entity/repository for review publication and audit metadata.
    - Add instructor endpoint for per-submission review command.
  - Interface:
    - Expose review fields in instructor history read response for each attempt.
  - Risks/open questions:
    - Legacy status mapping may affect pre-existing rows; migration must normalize old values.
  - Acceptance criteria:
    - Instructors can review any owned submission attempt with one of the 3 outcomes.
    - Review can be saved unpublished or published.
    - Review write stores reviewer identity and update/publish timestamps.

- [I2 - Student-visible status policy and legacy status migration]
  - Type: backend
  - Why: make student status semantics consistent with publication rules and remove auto-visible evaluator outcomes.
  - Domain:
    - Add `AWAITING_REVIEW` as student-facing fallback when no published review exists.
  - Application:
    - Update student answer status use case to resolve latest published review per question and fallback to `AWAITING_REVIEW`.
  - Infrastructure:
    - Add repository projection/query support for latest published review.
    - Add migration to map old statuses (`AWAITING_EVALUATION` -> `AWAITING_REVIEW`, `INCORRECT` -> `NEEDS_IMPROVEMENT`).
  - Interface:
    - API contracts return updated status values.
  - Risks/open questions:
    - Historical submissions without publication must correctly yield `AWAITING_REVIEW`.
  - Acceptance criteria:
    - Student status endpoint never leaks unpublished review outcome.
    - Questions with no published review show `AWAITING_REVIEW`.
    - Existing data migrates without invalid enum/string failures.

- [I3 - LLM draft generation and instructor acceptance workflow]
  - Type: backend
  - Why: integrate OpenRouter assistance without bypassing instructor control.
  - Domain:
    - Add submission behaviors for storing LLM draft review and accepting draft into current review.
    - Record acceptance audit metadata (accepted by/at).
  - Application:
    - Adjust async evaluation consumer to persist draft suggestion instead of directly publishing/evaluating student-visible result.
    - Add use case to accept LLM draft (optionally published immediately).
  - Infrastructure:
    - Add persistence fields for LLM draft payload/metadata.
    - Introduce OpenRouter provider adapter behind property switch with deterministic fallback.
    - Add instructor endpoint to accept LLM draft.
  - Interface:
    - Include pending LLM draft indicators/details in instructor answer history response.
  - Risks/open questions:
    - External API failure handling must not block submission flow.
  - Acceptance criteria:
    - LLM worker stores draft review only.
    - Instructor can accept draft and convert it into review.
    - Student still sees outcome only when review is published.

- [I4 - Instructor/student UI integration for review lifecycle]
  - Type: fullstack
  - Why: expose new capabilities in role-aware UX and keep status comprehension clear to both actors.
  - Domain:
    - No new domain behavior.
  - Application:
    - Wire frontend workspace services to new review and LLM-accept APIs.
  - Infrastructure:
    - No additional backend infrastructure.
  - Interface:
    - Instructor history panel displays attempts with review state, publication state, and LLM draft acceptance actions.
    - Student status labels render `AWAITING_REVIEW` and `NEEDS_IMPROVEMENT` terminology.
  - Risks/open questions:
    - Dense answer history UI may need careful layout to stay readable.
  - Acceptance criteria:
    - Instructor can create/edit/publish/unpublish manual review per attempt in UI.
    - Instructor can accept LLM draft from UI.
    - Student UI reflects new status vocabulary and publication-gated visibility.
