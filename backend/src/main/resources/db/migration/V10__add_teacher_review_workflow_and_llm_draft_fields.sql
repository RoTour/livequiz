ALTER TABLE submission_attempts
  ADD COLUMN review_published BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN review_created_at TIMESTAMPTZ,
  ADD COLUMN review_published_at TIMESTAMPTZ,
  ADD COLUMN reviewed_by_instructor_id VARCHAR(255),
  ADD COLUMN review_origin VARCHAR(64),
  ADD COLUMN llm_suggested_status VARCHAR(64),
  ADD COLUMN llm_suggested_comment TEXT,
  ADD COLUMN llm_suggested_missing_key_points TEXT,
  ADD COLUMN llm_suggested_at TIMESTAMPTZ,
  ADD COLUMN llm_suggested_model VARCHAR(255),
  ADD COLUMN llm_accepted_at TIMESTAMPTZ,
  ADD COLUMN llm_accepted_by_instructor_id VARCHAR(255);

UPDATE submission_attempts
SET answer_status = 'AWAITING_REVIEW'
WHERE answer_status = 'AWAITING_EVALUATION';

UPDATE submission_attempts
SET answer_status = 'NEEDS_IMPROVEMENT'
WHERE answer_status = 'INCORRECT';

ALTER TABLE submission_attempts
  ALTER COLUMN answer_status SET DEFAULT 'AWAITING_REVIEW';

UPDATE submission_attempts
SET
  review_published = TRUE,
  review_created_at = COALESCE(evaluation_completed_at, submitted_at),
  review_published_at = COALESCE(evaluation_completed_at, submitted_at),
  review_origin = 'LEGACY_AUTO'
WHERE answer_status IN ('CORRECT', 'INCOMPLETE', 'NEEDS_IMPROVEMENT');

CREATE INDEX idx_submission_attempts_review_published
  ON submission_attempts (review_published);

CREATE INDEX idx_submission_attempts_llm_suggested_status
  ON submission_attempts (llm_suggested_status);
