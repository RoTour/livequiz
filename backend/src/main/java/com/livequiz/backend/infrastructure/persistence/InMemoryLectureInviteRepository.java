package com.livequiz.backend.infrastructure.persistence;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile({ "in-memory", "memory" })
public class InMemoryLectureInviteRepository implements LectureInviteRepository {

  private final Map<String, LectureInvite> invitesById = new ConcurrentHashMap<>();

  @Override
  public void save(LectureInvite invite) {
    this.invitesById.put(invite.inviteId(), invite);
  }

  @Override
  public Optional<LectureInvite> findByInviteId(String inviteId) {
    return Optional.ofNullable(this.invitesById.get(inviteId));
  }

  @Override
  public List<LectureInvite> findByLectureId(LectureId lectureId) {
    return this.invitesById
      .values()
      .stream()
      .filter(invite -> invite.lectureId().value().equals(lectureId.value()))
      .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
      .toList();
  }

  @Override
  public Optional<LectureInvite> findActiveByTokenHash(String tokenHash, Instant now) {
    return this.invitesById
      .values()
      .stream()
      .filter(invite -> invite.tokenHash().equals(tokenHash))
      .filter(invite -> invite.isActiveAt(now))
      .findFirst();
  }

  @Override
  public Optional<LectureInvite> findLatestByTokenHash(String tokenHash) {
    return this.invitesById
      .values()
      .stream()
      .filter(invite -> invite.tokenHash().equals(tokenHash))
      .max(java.util.Comparator.comparing(LectureInvite::createdAt));
  }

  @Override
  public Optional<LectureInvite> findActiveByJoinCode(String joinCode, Instant now) {
    return this.invitesById
      .values()
      .stream()
      .filter(invite -> invite.joinCode().equals(joinCode))
      .filter(invite -> invite.isActiveAt(now))
      .findFirst();
  }

  @Override
  public Optional<LectureInvite> findLatestByJoinCode(String joinCode) {
    return this.invitesById
      .values()
      .stream()
      .filter(invite -> invite.joinCode().equals(joinCode))
      .max(java.util.Comparator.comparing(LectureInvite::createdAt));
  }

  @Override
  public boolean existsActiveByJoinCode(String joinCode, Instant now) {
    return this.invitesById
      .values()
      .stream()
      .filter(invite -> invite.joinCode().equals(joinCode))
      .anyMatch(invite -> invite.isActiveAt(now));
  }
}
