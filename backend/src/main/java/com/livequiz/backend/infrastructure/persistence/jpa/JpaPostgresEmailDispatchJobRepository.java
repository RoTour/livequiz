package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.application.messaging.EmailDispatchJob;
import com.livequiz.backend.application.messaging.EmailDispatchJobRepository;
import com.livequiz.backend.application.messaging.EmailDispatchStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresEmailDispatchJobRepository implements EmailDispatchJobRepository {

  private final JpaEmailDispatchJobRepository jpaEmailDispatchJobRepository;

  public JpaPostgresEmailDispatchJobRepository(
    JpaEmailDispatchJobRepository jpaEmailDispatchJobRepository
  ) {
    this.jpaEmailDispatchJobRepository = jpaEmailDispatchJobRepository;
  }

  @Override
  public void save(EmailDispatchJob emailDispatchJob) {
    this.jpaEmailDispatchJobRepository.save(toEntity(emailDispatchJob));
  }

  @Override
  public Optional<EmailDispatchJob> findByMessageId(String messageId) {
    return this.jpaEmailDispatchJobRepository.findById(messageId).map(this::toDomain);
  }

  @Override
  public boolean claimForProcessing(String messageId, Instant now) {
    return this.jpaEmailDispatchJobRepository.claimForProcessing(messageId, now) > 0;
  }

  private EmailDispatchJobEntity toEntity(EmailDispatchJob emailDispatchJob) {
    return new EmailDispatchJobEntity(
      emailDispatchJob.messageId(),
      emailDispatchJob.toEmail(),
      emailDispatchJob.verificationToken(),
      emailDispatchJob.verificationUrl(),
      emailDispatchJob.expiresAt(),
      emailDispatchJob.status().name(),
      emailDispatchJob.attemptCount(),
      emailDispatchJob.nextAttemptAt(),
      emailDispatchJob.lastError(),
      emailDispatchJob.createdAt(),
      emailDispatchJob.updatedAt()
    );
  }

  private EmailDispatchJob toDomain(EmailDispatchJobEntity entity) {
    return new EmailDispatchJob(
      entity.getMessageId(),
      entity.getToEmail(),
      entity.getVerificationToken(),
      entity.getVerificationUrl(),
      entity.getExpiresAt(),
      EmailDispatchStatus.valueOf(entity.getStatus()),
      entity.getAttemptCount(),
      entity.getNextAttemptAt(),
      entity.getLastError(),
      entity.getCreatedAt(),
      entity.getUpdatedAt()
    );
  }
}
