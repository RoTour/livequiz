package com.livequiz.backend.infrastructure.persistence.jpa;

import java.time.LocalDate;
import java.time.Instant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaEmailDailyQuotaUsageRepository
  extends JpaRepository<EmailDailyQuotaUsageEntity, LocalDate> {
  @Modifying
  @Query(
    value =
    """
      update email_daily_quota_usage
      set sent_count = sent_count + 1,
          updated_at = :updatedAt
      where quota_date = :quotaDate
        and sent_count < :maxPerDay
    """,
    nativeQuery = true
  )
  int reserveExistingQuota(
    @Param("quotaDate") LocalDate quotaDate,
    @Param("maxPerDay") int maxPerDay,
    @Param("updatedAt") Instant updatedAt
  );
}
