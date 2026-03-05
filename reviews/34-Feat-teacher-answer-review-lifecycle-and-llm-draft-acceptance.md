# 34 - Feat teacher answer review lifecycle and llm draft acceptance

## Why

Automatic evaluator outcomes were being shown to students without explicit instructor approval, which prevented teachers from controlling per-attempt feedback quality and visibility. The review workflow now centers instructor decisions, keeps AI output as advisory draft data, and only exposes published outcomes to students.

## What changed

- Added submission review lifecycle in domain + persistence:
  - replaced legacy statuses (`AWAITING_EVALUATION`, `INCORRECT`) with `AWAITING_REVIEW` and `NEEDS_IMPROVEMENT`,
  - added manual review upsert and LLM draft acceptance behaviors,
  - added review metadata fields (published state, origin, reviewer, timestamps, LLM suggestion/acceptance metadata),
  - added migration `V10__add_teacher_review_workflow_and_llm_draft_fields.sql` with legacy status normalization.
- Added instructor review application APIs:
  - `UpsertSubmissionManualReviewUseCase`,
  - `AcceptSubmissionLlmReviewUseCase`,
  - `GetQuestionSubmissionReviewsUseCase` for attempt-level instructor history including draft AI suggestion details.
- Extended instructor controller surface:
  - `GET /api/lectures/{lectureId}/questions/{questionId}/answers/reviews`,
  - `PUT /api/lectures/{lectureId}/questions/{questionId}/answers/{submissionId}/review`,
  - `POST /api/lectures/{lectureId}/questions/{questionId}/answers/{submissionId}/llm-review/accept`.
- Updated async evaluation pipeline:
  - evaluator providers now include model metadata,
  - deterministic evaluator is property-gated and emits `NEEDS_IMPROVEMENT` instead of `INCORRECT`,
  - consumer stores evaluator output as LLM suggestion draft instead of directly publishing student-visible feedback,
  - added OpenRouter provider behind `livequiz.answer-evaluation.provider=openrouter`.
- Updated student-visible status policy:
  - submit response starts at `AWAITING_REVIEW`,
  - student status projection resolves from latest published review only and falls back to `AWAITING_REVIEW` when none is published.
- Updated frontend instructor/student integration:
  - lecture service + workspace APIs for review fetch/upsert/LLM acceptance,
  - instructor history panel now renders per-attempt review controls and AI suggestion acceptance,
  - student chip mapping updated to `AWAITING_REVIEW` and `NEEDS_IMPROVEMENT` labels.
- Added integration coverage for:
  - manual review draft/publish lifecycle,
  - student visibility gate for unpublished reviews,
  - instructor acceptance of LLM suggestions.

## Rollout note

- Default evaluator remains deterministic via `LIVEQUIZ_ANSWER_EVALUATION_PROVIDER=deterministic`.
- To enable OpenRouter drafts, set:
  - `LIVEQUIZ_ANSWER_EVALUATION_PROVIDER=openrouter`,
  - `LIVEQUIZ_ANSWER_EVALUATION_OPENROUTER_API_KEY`, and optional model/app/site overrides.
