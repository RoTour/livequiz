CREATE TABLE outbox_messages (
  id VARCHAR(255) PRIMARY KEY,
  event_type VARCHAR(255) NOT NULL,
  routing_key VARCHAR(255) NOT NULL,
  payload_json TEXT NOT NULL,
  correlation_id VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL,
  published_at TIMESTAMPTZ,
  attempt_count INTEGER NOT NULL DEFAULT 0,
  last_error TEXT
);

CREATE INDEX idx_outbox_messages_pending_created_at
  ON outbox_messages (created_at)
  WHERE published_at IS NULL;

CREATE TABLE email_dispatch_jobs (
  message_id VARCHAR(255) PRIMARY KEY,
  to_email VARCHAR(320) NOT NULL,
  verification_token VARCHAR(1024) NOT NULL,
  verification_url TEXT NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(64) NOT NULL,
  attempt_count INTEGER NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMPTZ,
  last_error TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT chk_email_dispatch_jobs_status
    CHECK (status IN ('QUEUED', 'SENT', 'RETRY_SCHEDULED', 'FAILED_FINAL'))
);

CREATE INDEX idx_email_dispatch_jobs_status_next_attempt
  ON email_dispatch_jobs (status, next_attempt_at);

CREATE TABLE email_daily_quota_usage (
  quota_date DATE PRIMARY KEY,
  sent_count INTEGER NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT chk_email_daily_quota_usage_non_negative
    CHECK (sent_count >= 0)
);

CREATE TABLE submission_evaluation_jobs (
  submission_id VARCHAR(255) PRIMARY KEY,
  lecture_id VARCHAR(255) NOT NULL,
  question_id VARCHAR(255) NOT NULL,
  student_id VARCHAR(255) NOT NULL,
  status VARCHAR(64) NOT NULL,
  attempt_count INTEGER NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMPTZ,
  last_error TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT fk_submission_evaluation_jobs_submission
    FOREIGN KEY (submission_id) REFERENCES submission_attempts(id) ON DELETE CASCADE,
  CONSTRAINT chk_submission_evaluation_jobs_status
    CHECK (status IN ('QUEUED', 'PROCESSING', 'RETRY_SCHEDULED', 'FAILED_FINAL', 'COMPLETED'))
);

CREATE INDEX idx_submission_evaluation_jobs_status_next_attempt
  ON submission_evaluation_jobs (status, next_attempt_at);

ALTER TABLE submission_attempts
  ADD COLUMN answer_status VARCHAR(64) NOT NULL DEFAULT 'AWAITING_EVALUATION',
  ADD COLUMN evaluation_completed_at TIMESTAMPTZ,
  ADD COLUMN feedback_is_correct BOOLEAN,
  ADD COLUMN feedback_comment TEXT,
  ADD COLUMN feedback_missing_key_points TEXT;

CREATE INDEX idx_submission_attempts_answer_status
  ON submission_attempts (answer_status);
