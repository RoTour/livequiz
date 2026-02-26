package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.application.messaging.EmailDailyQuotaUsageRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryEmailDailyQuotaUsageRepository
  implements EmailDailyQuotaUsageRepository {

  private final Map<LocalDate, Integer> sentCountByDate = new ConcurrentHashMap<>();

  @Override
  public synchronized boolean tryReserve(LocalDate quotaDate, int maxPerDay, Instant now) {
    int current = this.sentCountByDate.getOrDefault(quotaDate, 0);
    if (current >= maxPerDay) {
      return false;
    }
    this.sentCountByDate.put(quotaDate, current + 1);
    return true;
  }
}
