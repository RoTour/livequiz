package com.livequiz.backend.domain.lecture;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LectureInviteRepository {
  void save(LectureInvite invite);

  Optional<LectureInvite> findByInviteId(String inviteId);

  List<LectureInvite> findByLectureId(LectureId lectureId);

  Optional<LectureInvite> findActiveByTokenHash(String tokenHash, Instant now);

  Optional<LectureInvite> findLatestByTokenHash(String tokenHash);

  Optional<LectureInvite> findActiveByJoinCode(String joinCode, Instant now);

  Optional<LectureInvite> findLatestByJoinCode(String joinCode);

  boolean existsActiveByJoinCode(String joinCode, Instant now);
}
