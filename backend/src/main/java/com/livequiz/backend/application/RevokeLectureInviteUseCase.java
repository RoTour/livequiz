package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RevokeLectureInviteUseCase {

  private final LectureInviteRepository lectureInviteRepository;

  public RevokeLectureInviteUseCase(LectureInviteRepository lectureInviteRepository) {
    this.lectureInviteRepository = lectureInviteRepository;
  }

  public LectureInvite execute(String lectureId, String inviteId) {
    LectureInvite invite = this.lectureInviteRepository
      .findByInviteId(inviteId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND", "Invite not found")
      );

    if (!invite.lectureId().value().equals(lectureId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND", "Invite not found");
    }

    LectureInvite revokedInvite = invite.revoke(Instant.now());
    this.lectureInviteRepository.save(revokedInvite);
    return revokedInvite;
  }
}
