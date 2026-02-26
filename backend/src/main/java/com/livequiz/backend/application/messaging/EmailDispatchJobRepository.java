package com.livequiz.backend.application.messaging;

import java.util.Optional;
import java.time.Instant;

public interface EmailDispatchJobRepository {
  void save(EmailDispatchJob emailDispatchJob);

  Optional<EmailDispatchJob> findByMessageId(String messageId);

  boolean claimForProcessing(String messageId, Instant now);
}
