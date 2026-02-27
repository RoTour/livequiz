CREATE TABLE instructor_accounts (
  email VARCHAR(320) PRIMARY KEY,
  password_hash VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT chk_instructor_accounts_email_format CHECK (POSITION('@' IN email) > 1),
  CONSTRAINT chk_instructor_accounts_updated_after_created
    CHECK (updated_at >= created_at)
);

CREATE INDEX idx_instructor_accounts_active
  ON instructor_accounts (active);
