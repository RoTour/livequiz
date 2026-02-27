INSERT INTO teacher_identities (principal_id, active, created_at, updated_at)
SELECT DISTINCT ON (normalized_principal)
  normalized_principal,
  active,
  created_at,
  legacy_updated_at
FROM (
  SELECT
    LOWER(BTRIM(principal_id)) || '@ynov.com' AS normalized_principal,
    active,
    created_at,
    COALESCE(updated_at, created_at, NOW()) AS legacy_updated_at
  FROM teacher_identities
  WHERE principal_id IS NOT NULL
    AND BTRIM(principal_id) <> ''
    AND POSITION('@' IN BTRIM(principal_id)) = 0
) legacy_principals
ORDER BY normalized_principal, legacy_updated_at DESC
ON CONFLICT (principal_id)
DO UPDATE
SET active = EXCLUDED.active,
    updated_at = EXCLUDED.updated_at
WHERE teacher_identities.updated_at IS NULL
  OR EXCLUDED.updated_at > teacher_identities.updated_at;

DELETE FROM teacher_identities
WHERE principal_id IS NOT NULL
  AND POSITION('@' IN principal_id) = 0;

UPDATE lectures
SET created_by_instructor_id = LOWER(BTRIM(created_by_instructor_id)) || '@ynov.com'
WHERE created_by_instructor_id IS NOT NULL
  AND BTRIM(created_by_instructor_id) <> ''
  AND POSITION('@' IN BTRIM(created_by_instructor_id)) = 0;

UPDATE lecture_invites
SET created_by_instructor_id = LOWER(BTRIM(created_by_instructor_id)) || '@ynov.com'
WHERE created_by_instructor_id IS NOT NULL
  AND BTRIM(created_by_instructor_id) <> ''
  AND POSITION('@' IN BTRIM(created_by_instructor_id)) = 0;
