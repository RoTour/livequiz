package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.application.messaging.EmailDispatchJob;
import com.livequiz.backend.application.messaging.EmailDispatchJobRepository;
import com.livequiz.backend.application.messaging.EmailDispatchStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryEmailDispatchJobRepository implements EmailDispatchJobRepository {

  private final Map<String, EmailDispatchJob> jobsByMessageId = new ConcurrentHashMap<>();

  @Override
  public void save(EmailDispatchJob emailDispatchJob) {
    this.jobsByMessageId.put(emailDispatchJob.messageId(), emailDispatchJob);
  }

  @Override
  public Optional<EmailDispatchJob> findByMessageId(String messageId) {
    return Optional.ofNullable(this.jobsByMessageId.get(messageId));
  }

  @Override
  public synchronized boolean claimForProcessing(String messageId, Instant now) {
    EmailDispatchJob existing = this.jobsByMessageId.get(messageId);
    if (existing == null) {
      return false;
    }
    if (
      existing.status() != EmailDispatchStatus.QUEUED &&
      existing.status() != EmailDispatchStatus.RETRY_SCHEDULED
    ) {
      return false;
    }
    this.jobsByMessageId.put(messageId, existing.markProcessing(now));
    return true;
  }
}
