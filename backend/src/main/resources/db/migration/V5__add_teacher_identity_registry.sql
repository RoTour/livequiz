CREATE TABLE teacher_identities (
  principal_id VARCHAR(255) PRIMARY KEY,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT chk_teacher_identities_updated_after_created
    CHECK (updated_at >= created_at)
);

CREATE INDEX idx_teacher_identities_active
  ON teacher_identities (active);

INSERT INTO teacher_identities (principal_id, active, created_at, updated_at)
VALUES
  ('instructor', TRUE, NOW(), NOW()),
  ('instructor2', TRUE, NOW(), NOW());
