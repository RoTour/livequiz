package com.livequiz.backend.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLectureInviteRepository
  extends JpaRepository<LectureInviteEntity, String> {
  List<LectureInviteEntity> findByLectureIdOrderByCreatedAtDesc(String lectureId);

  Optional<LectureInviteEntity> findFirstByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
    String tokenHash,
    Instant now
  );

  Optional<LectureInviteEntity> findFirstByTokenHashOrderByCreatedAtDesc(String tokenHash);

  Optional<LectureInviteEntity> findFirstByJoinCodeAndRevokedAtIsNullAndExpiresAtAfter(
    String joinCode,
    Instant now
  );

  Optional<LectureInviteEntity> findFirstByJoinCodeOrderByCreatedAtDesc(String joinCode);

  boolean existsByJoinCodeAndRevokedAtIsNullAndExpiresAtAfter(String joinCode, Instant now);
}
