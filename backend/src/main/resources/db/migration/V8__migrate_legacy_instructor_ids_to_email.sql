UPDATE lectures
SET created_by_instructor_id = 'instructor@ynov.com'
WHERE created_by_instructor_id = 'instructor';

UPDATE lectures
SET created_by_instructor_id = 'instructor2@ynov.com'
WHERE created_by_instructor_id = 'instructor2';

UPDATE lecture_invites
SET created_by_instructor_id = 'instructor@ynov.com'
WHERE created_by_instructor_id = 'instructor';

UPDATE lecture_invites
SET created_by_instructor_id = 'instructor2@ynov.com'
WHERE created_by_instructor_id = 'instructor2';
