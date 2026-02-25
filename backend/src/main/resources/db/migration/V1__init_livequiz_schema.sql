CREATE TABLE lectures (
  id VARCHAR(255) PRIMARY KEY,
  title VARCHAR(1000) NOT NULL,
  created_by_instructor_id VARCHAR(255),
  created_at TIMESTAMPTZ
);

CREATE INDEX idx_lectures_owner_created_at
  ON lectures (created_by_instructor_id, created_at DESC);

CREATE TABLE lecture_questions (
  lecture_id VARCHAR(255) NOT NULL,
  question_id VARCHAR(255) NOT NULL,
  prompt TEXT NOT NULL,
  model_answer TEXT NOT NULL,
  time_limit_seconds INTEGER NOT NULL,
  question_order INTEGER NOT NULL,
  PRIMARY KEY (lecture_id, question_id),
  CONSTRAINT fk_lecture_questions_lecture
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE,
  CONSTRAINT uq_lecture_questions_order
    UNIQUE (lecture_id, question_order),
  CONSTRAINT chk_lecture_questions_time_limit
    CHECK (time_limit_seconds > 0),
  CONSTRAINT chk_lecture_questions_order
    CHECK (question_order > 0)
);

CREATE INDEX idx_lecture_questions_lookup
  ON lecture_questions (lecture_id, question_order);

CREATE TABLE lecture_unlocked_questions (
  lecture_id VARCHAR(255) NOT NULL,
  question_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (lecture_id, question_id),
  CONSTRAINT fk_lecture_unlocked_questions_lecture
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

CREATE INDEX idx_lecture_unlocked_questions_lecture
  ON lecture_unlocked_questions (lecture_id);

CREATE TABLE lecture_invites (
  id VARCHAR(255) PRIMARY KEY,
  lecture_id VARCHAR(255) NOT NULL,
  created_by_instructor_id VARCHAR(255) NOT NULL,
  join_code VARCHAR(16) NOT NULL,
  token_hash VARCHAR(128) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  CONSTRAINT fk_lecture_invites_lecture
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE,
  CONSTRAINT chk_lecture_invites_expires_after_created
    CHECK (expires_at > created_at)
);

CREATE INDEX idx_lecture_invites_lecture_created_at
  ON lecture_invites (lecture_id, created_at DESC);

CREATE INDEX idx_lecture_invites_token_created_at
  ON lecture_invites (token_hash, created_at DESC);

CREATE INDEX idx_lecture_invites_join_code_created_at
  ON lecture_invites (join_code, created_at DESC);

CREATE INDEX idx_lecture_invites_join_code_active
  ON lecture_invites (join_code, revoked_at, expires_at);

CREATE INDEX idx_lecture_invites_token_active
  ON lecture_invites (token_hash, revoked_at, expires_at);

CREATE TABLE lecture_enrollments (
  lecture_id VARCHAR(255) NOT NULL,
  student_id VARCHAR(255) NOT NULL,
  enrolled_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (lecture_id, student_id),
  CONSTRAINT fk_lecture_enrollments_lecture
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

CREATE INDEX idx_lecture_enrollments_student_enrolled_at
  ON lecture_enrollments (student_id, enrolled_at DESC);

CREATE TABLE submission_attempts (
  id VARCHAR(255) PRIMARY KEY,
  lecture_id VARCHAR(255) NOT NULL,
  question_id VARCHAR(255) NOT NULL,
  student_id VARCHAR(255) NOT NULL,
  submitted_at TIMESTAMPTZ NOT NULL,
  answer_text TEXT NOT NULL,
  CONSTRAINT fk_submission_attempts_lecture
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

CREATE INDEX idx_submission_attempts_latest
  ON submission_attempts (lecture_id, question_id, student_id, submitted_at DESC);

CREATE INDEX idx_submission_attempts_lecture_question
  ON submission_attempts (lecture_id, question_id);

CREATE INDEX idx_submission_attempts_lecture_student_question
  ON submission_attempts (lecture_id, student_id, question_id);
