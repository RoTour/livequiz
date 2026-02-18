package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureEnrollment;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class JoinLectureUseCase {

  public record JoinResult(String lectureId, String studentId, boolean alreadyEnrolled, Instant enrolledAt) {}

  private final LectureInviteRepository lectureInviteRepository;
  private final LectureEnrollmentRepository lectureEnrollmentRepository;
  private final LectureRepository lectureRepository;
  private final InviteTokenService inviteTokenService;

  public JoinLectureUseCase(
    LectureInviteRepository lectureInviteRepository,
    LectureEnrollmentRepository lectureEnrollmentRepository,
    LectureRepository lectureRepository,
    InviteTokenService inviteTokenService
  ) {
    this.lectureInviteRepository = lectureInviteRepository;
    this.lectureEnrollmentRepository = lectureEnrollmentRepository;
    this.lectureRepository = lectureRepository;
    this.inviteTokenService = inviteTokenService;
  }

  public JoinResult execute(String token, String joinCode, String studentId) {
    if ((token == null || token.isBlank()) && (joinCode == null || joinCode.isBlank())) {
      throw new ApiException(
        HttpStatus.BAD_REQUEST,
        "INVITE_CREDENTIALS_REQUIRED",
        "Either token or code is required"
      );
    }

    Instant now = Instant.now();
    LectureInvite invite;
    if (token != null && !token.isBlank()) {
      String tokenHash = this.inviteTokenService.hashToken(token);
      invite = this.lectureInviteRepository
        .findActiveByTokenHash(tokenHash, now)
        .orElseThrow(() ->
          new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND", "Invite not found")
        );
    } else {
      invite = this.lectureInviteRepository
        .findActiveByJoinCode(joinCode, now)
        .orElseThrow(() ->
          new ApiException(HttpStatus.NOT_FOUND, "INVITE_NOT_FOUND", "Invite not found")
        );
    }

    this.lectureRepository
      .findById(invite.lectureId())
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );

    var existingEnrollment = this.lectureEnrollmentRepository.findByLectureIdAndStudentId(
      invite.lectureId(),
      studentId
    );
    boolean alreadyEnrolled = existingEnrollment.isPresent();
    Instant enrolledAt = existingEnrollment.map(LectureEnrollment::enrolledAt).orElse(now);
    if (!alreadyEnrolled) {
      LectureEnrollment enrollment = new LectureEnrollment(
        invite.lectureId(),
        studentId,
        enrolledAt
      );
      this.lectureEnrollmentRepository.save(enrollment);
    }

    return new JoinResult(invite.lectureId().value(), studentId, alreadyEnrolled, enrolledAt);
  }
}
