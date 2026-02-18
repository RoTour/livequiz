package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.application.CreateLectureInviteUseCase;
import com.livequiz.backend.application.CurrentUserService;
import com.livequiz.backend.application.ListLectureInvitesUseCase;
import com.livequiz.backend.application.RevokeLectureInviteUseCase;
import com.livequiz.backend.domain.lecture.LectureInvite;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectures/{lectureId}/invites")
public class LectureInviteController {

  private final CreateLectureInviteUseCase createLectureInviteUseCase;
  private final ListLectureInvitesUseCase listLectureInvitesUseCase;
  private final RevokeLectureInviteUseCase revokeLectureInviteUseCase;
  private final CurrentUserService currentUserService;

  public LectureInviteController(
    CreateLectureInviteUseCase createLectureInviteUseCase,
    ListLectureInvitesUseCase listLectureInvitesUseCase,
    RevokeLectureInviteUseCase revokeLectureInviteUseCase,
    CurrentUserService currentUserService
  ) {
    this.createLectureInviteUseCase = createLectureInviteUseCase;
    this.listLectureInvitesUseCase = listLectureInvitesUseCase;
    this.revokeLectureInviteUseCase = revokeLectureInviteUseCase;
    this.currentUserService = currentUserService;
  }

  @PostMapping
  public CreateInviteResponse createInvite(@PathVariable String lectureId) {
    String instructorId = this.currentUserService.requireUserId();
    CreateLectureInviteUseCase.CreateInviteResult result = this.createLectureInviteUseCase.execute(
      lectureId,
      instructorId
    );
    return CreateInviteResponse.from(result.invite(), result.joinUrl());
  }

  @GetMapping
  public List<InviteResponse> listInvites(@PathVariable String lectureId) {
    return this.listLectureInvitesUseCase
      .execute(lectureId)
      .stream()
      .map(InviteResponse::from)
      .toList();
  }

  @PostMapping("/{inviteId}/revoke")
  public InviteResponse revokeInvite(
    @PathVariable String lectureId,
    @PathVariable String inviteId
  ) {
    LectureInvite invite = this.revokeLectureInviteUseCase.execute(lectureId, inviteId);
    return InviteResponse.from(invite);
  }

  public record CreateInviteResponse(
    String inviteId,
    String lectureId,
    String joinCode,
    String joinUrl,
    Instant expiresAt,
    boolean active
  ) {
    private static CreateInviteResponse from(LectureInvite invite, String joinUrl) {
      return new CreateInviteResponse(
        invite.inviteId(),
        invite.lectureId().value(),
        invite.joinCode(),
        joinUrl,
        invite.expiresAt(),
        invite.isActiveAt(Instant.now())
      );
    }
  }

  public record InviteResponse(
    String inviteId,
    String lectureId,
    String joinCode,
    Instant createdAt,
    Instant expiresAt,
    Instant revokedAt,
    boolean active
  ) {
    private static InviteResponse from(LectureInvite invite) {
      return new InviteResponse(
        invite.inviteId(),
        invite.lectureId().value(),
        invite.joinCode(),
        invite.createdAt(),
        invite.expiresAt(),
        invite.revokedAt(),
        invite.isActiveAt(Instant.now())
      );
    }
  }
}
