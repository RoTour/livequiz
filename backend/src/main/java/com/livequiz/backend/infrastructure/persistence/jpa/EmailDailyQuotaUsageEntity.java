package com.livequiz.backend.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "email_daily_quota_usage")
public class EmailDailyQuotaUsageEntity {

  @Id
  private LocalDate quotaDate;

  @Column(nullable = false)
  private int sentCount;

  @Column(nullable = false)
  private Instant updatedAt;

  public EmailDailyQuotaUsageEntity() {}

  public EmailDailyQuotaUsageEntity(LocalDate quotaDate, int sentCount, Instant updatedAt) {
    this.quotaDate = quotaDate;
    this.sentCount = sentCount;
    this.updatedAt = updatedAt;
  }

  public LocalDate getQuotaDate() {
    return quotaDate;
  }

  public int getSentCount() {
    return sentCount;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
