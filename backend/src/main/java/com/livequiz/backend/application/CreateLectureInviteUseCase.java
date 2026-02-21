package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateLectureInviteUseCase {

  public record CreateInviteResult(
    LectureInvite invite,
    String token,
    String joinUrl
  ) {}

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final LectureInviteRepository lectureInviteRepository;
  private final InviteTokenService inviteTokenService;
  private final LiveQuizProperties liveQuizProperties;

  public CreateLectureInviteUseCase(
    InstructorLectureAccessService instructorLectureAccessService,
    LectureInviteRepository lectureInviteRepository,
    InviteTokenService inviteTokenService,
    LiveQuizProperties liveQuizProperties
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
    this.lectureInviteRepository = lectureInviteRepository;
    this.inviteTokenService = inviteTokenService;
    this.liveQuizProperties = liveQuizProperties;
  }

  public CreateInviteResult execute(String lectureId, String instructorId) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    this.instructorLectureAccessService.getOwnedLectureOrThrow(resolvedLectureId);

    Instant now = Instant.now();
    String joinCode;
    do {
      joinCode = this.inviteTokenService.generateJoinCode();
    } while (this.lectureInviteRepository.existsActiveByJoinCode(joinCode, now));

    String token = this.inviteTokenService.generateOpaqueToken();
    String tokenHash = this.inviteTokenService.hashToken(token);
    LectureInvite invite = new LectureInvite(
      UUID.randomUUID().toString(),
      resolvedLectureId,
      instructorId,
      joinCode,
      tokenHash,
      now,
      now.plus(Duration.ofHours(this.liveQuizProperties.inviteExpirationHours())),
      null
    );
    this.lectureInviteRepository.save(invite);

    String joinUrl = buildJoinUrl(token);
    return new CreateInviteResult(invite, token, joinUrl);
  }

  private String buildJoinUrl(String token) {
    String baseUrl = this.liveQuizProperties.inviteBaseUrl();
    if (baseUrl.endsWith("/")) {
      return baseUrl + token;
    }
    return baseUrl + "/" + token;
  }
}
