package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CreateLectureInviteUseCase {

  public record CreateInviteResult(
    LectureInvite invite,
    String token,
    String joinUrl
  ) {}

  private final LectureRepository lectureRepository;
  private final LectureInviteRepository lectureInviteRepository;
  private final InviteTokenService inviteTokenService;
  private final LiveQuizProperties liveQuizProperties;

  public CreateLectureInviteUseCase(
    LectureRepository lectureRepository,
    LectureInviteRepository lectureInviteRepository,
    InviteTokenService inviteTokenService,
    LiveQuizProperties liveQuizProperties
  ) {
    this.lectureRepository = lectureRepository;
    this.lectureInviteRepository = lectureInviteRepository;
    this.inviteTokenService = inviteTokenService;
    this.liveQuizProperties = liveQuizProperties;
  }

  public CreateInviteResult execute(String lectureId, String instructorId) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    this.lectureRepository
      .findById(resolvedLectureId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

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

    String joinUrl = this.liveQuizProperties.inviteBaseUrl() + "?token=" + token;
    return new CreateInviteResult(invite, token, joinUrl);
  }
}
