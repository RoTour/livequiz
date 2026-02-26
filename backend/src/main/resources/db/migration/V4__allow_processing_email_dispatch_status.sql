ALTER TABLE email_dispatch_jobs
  DROP CONSTRAINT chk_email_dispatch_jobs_status;

ALTER TABLE email_dispatch_jobs
  ADD CONSTRAINT chk_email_dispatch_jobs_status
  CHECK (status IN ('QUEUED', 'PROCESSING', 'SENT', 'RETRY_SCHEDULED', 'FAILED_FINAL'));
