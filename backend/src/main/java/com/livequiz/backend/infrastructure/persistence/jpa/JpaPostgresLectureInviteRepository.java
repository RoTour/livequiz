package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresLectureInviteRepository implements LectureInviteRepository {

  private final JpaLectureInviteRepository jpaLectureInviteRepository;

  public JpaPostgresLectureInviteRepository(
    JpaLectureInviteRepository jpaLectureInviteRepository
  ) {
    this.jpaLectureInviteRepository = jpaLectureInviteRepository;
  }

  @Override
  public void save(LectureInvite invite) {
    this.jpaLectureInviteRepository.save(toEntity(invite));
  }

  @Override
  public Optional<LectureInvite> findByInviteId(String inviteId) {
    return this.jpaLectureInviteRepository.findById(inviteId).map(this::toDomain);
  }

  @Override
  public List<LectureInvite> findByLectureId(LectureId lectureId) {
    return this.jpaLectureInviteRepository
      .findByLectureIdOrderByCreatedAtDesc(lectureId.value())
      .stream()
      .map(this::toDomain)
      .toList();
  }

  @Override
  public Optional<LectureInvite> findActiveByTokenHash(String tokenHash, Instant now) {
    return this.jpaLectureInviteRepository
      .findFirstByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)
      .map(this::toDomain);
  }

  @Override
  public Optional<LectureInvite> findLatestByTokenHash(String tokenHash) {
    return this.jpaLectureInviteRepository
      .findFirstByTokenHashOrderByCreatedAtDesc(tokenHash)
      .map(this::toDomain);
  }

  @Override
  public Optional<LectureInvite> findActiveByJoinCode(String joinCode, Instant now) {
    return this.jpaLectureInviteRepository
      .findFirstByJoinCodeAndRevokedAtIsNullAndExpiresAtAfter(joinCode, now)
      .map(this::toDomain);
  }

  @Override
  public Optional<LectureInvite> findLatestByJoinCode(String joinCode) {
    return this.jpaLectureInviteRepository
      .findFirstByJoinCodeOrderByCreatedAtDesc(joinCode)
      .map(this::toDomain);
  }

  @Override
  public boolean existsActiveByJoinCode(String joinCode, Instant now) {
    return this.jpaLectureInviteRepository.existsByJoinCodeAndRevokedAtIsNullAndExpiresAtAfter(
        joinCode,
        now
      );
  }

  private LectureInviteEntity toEntity(LectureInvite invite) {
    return new LectureInviteEntity(
      invite.inviteId(),
      invite.lectureId().value(),
      invite.createdByInstructorId(),
      invite.joinCode(),
      invite.tokenHash(),
      invite.createdAt(),
      invite.expiresAt(),
      invite.revokedAt()
    );
  }

  private LectureInvite toDomain(LectureInviteEntity entity) {
    return new LectureInvite(
      entity.getId(),
      new LectureId(entity.getLectureId()),
      entity.getCreatedByInstructorId(),
      entity.getJoinCode(),
      entity.getTokenHash(),
      entity.getCreatedAt(),
      entity.getExpiresAt(),
      entity.getRevokedAt()
    );
  }
}
