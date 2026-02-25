CREATE TABLE student_identities (
  student_id VARCHAR(255) PRIMARY KEY,
  email VARCHAR(320),
  status VARCHAR(64) NOT NULL,
  email_verified_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uq_student_identities_email UNIQUE (email)
);

CREATE INDEX idx_student_identities_status
  ON student_identities (status);

CREATE TABLE email_verification_challenges (
  challenge_id VARCHAR(255) PRIMARY KEY,
  student_id VARCHAR(255) NOT NULL,
  email VARCHAR(320) NOT NULL,
  token_hash VARCHAR(128) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  consumed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT fk_email_verification_challenges_student
    FOREIGN KEY (student_id) REFERENCES student_identities(student_id) ON DELETE CASCADE,
  CONSTRAINT chk_email_verification_challenges_expires_after_created
    CHECK (expires_at > created_at)
);

CREATE INDEX idx_email_verification_challenges_student_created_at
  ON email_verification_challenges (student_id, created_at DESC);

CREATE INDEX idx_email_verification_challenges_token_hash_created_at
  ON email_verification_challenges (token_hash, created_at DESC);

CREATE INDEX idx_email_verification_challenges_active_window
  ON email_verification_challenges (expires_at, consumed_at);
