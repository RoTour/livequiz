package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListLectureInvitesUseCase {

  private final InstructorLectureAccessService instructorLectureAccessService;
  private final LectureInviteRepository lectureInviteRepository;

  public ListLectureInvitesUseCase(
    InstructorLectureAccessService instructorLectureAccessService,
    LectureInviteRepository lectureInviteRepository
  ) {
    this.instructorLectureAccessService = instructorLectureAccessService;
    this.lectureInviteRepository = lectureInviteRepository;
  }

  public List<LectureInvite> execute(String lectureId) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    this.instructorLectureAccessService.getOwnedLectureOrThrow(resolvedLectureId);
    return this.lectureInviteRepository.findByLectureId(resolvedLectureId);
  }
}
