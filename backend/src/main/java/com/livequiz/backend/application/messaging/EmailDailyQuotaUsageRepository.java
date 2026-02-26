package com.livequiz.backend.application.messaging;

import java.time.Instant;
import java.time.LocalDate;

public interface EmailDailyQuotaUsageRepository {
  boolean tryReserve(LocalDate quotaDate, int maxPerDay, Instant now);
}
