INSERT INTO teacher_identities (principal_id, active, created_at, updated_at)
VALUES
  ('instructor@ynov.com', TRUE, NOW(), NOW()),
  ('instructor2@ynov.com', TRUE, NOW(), NOW())
ON CONFLICT (principal_id) DO NOTHING;

UPDATE teacher_identities
SET active = TRUE,
    updated_at = NOW()
WHERE principal_id IN ('instructor@ynov.com', 'instructor2@ynov.com');

DELETE FROM teacher_identities
WHERE principal_id IN ('instructor', 'instructor2');
