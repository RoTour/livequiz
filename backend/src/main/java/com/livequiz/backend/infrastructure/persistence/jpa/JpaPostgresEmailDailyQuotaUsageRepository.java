package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.application.messaging.EmailDailyQuotaUsageRepository;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("postgres")
public class JpaPostgresEmailDailyQuotaUsageRepository
  implements EmailDailyQuotaUsageRepository {

  private final JpaEmailDailyQuotaUsageRepository jpaEmailDailyQuotaUsageRepository;

  public JpaPostgresEmailDailyQuotaUsageRepository(
    JpaEmailDailyQuotaUsageRepository jpaEmailDailyQuotaUsageRepository
  ) {
    this.jpaEmailDailyQuotaUsageRepository = jpaEmailDailyQuotaUsageRepository;
  }

  @Override
  @Transactional
  public boolean tryReserve(LocalDate quotaDate, int maxPerDay, Instant now) {
    int updatedExisting = this.jpaEmailDailyQuotaUsageRepository.reserveExistingQuota(
      quotaDate,
      maxPerDay,
      now
    );
    if (updatedExisting > 0) {
      return true;
    }

    try {
      this.jpaEmailDailyQuotaUsageRepository.save(new EmailDailyQuotaUsageEntity(quotaDate, 1, now));
      return true;
    } catch (DataIntegrityViolationException ignored) {
      return (
        this.jpaEmailDailyQuotaUsageRepository.reserveExistingQuota(quotaDate, maxPerDay, now) >
        0
      );
    }
  }
}
