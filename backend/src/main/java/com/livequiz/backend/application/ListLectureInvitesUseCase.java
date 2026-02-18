package com.livequiz.backend.application;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import com.livequiz.backend.domain.lecture.LectureRepository;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ListLectureInvitesUseCase {

  private final LectureRepository lectureRepository;
  private final LectureInviteRepository lectureInviteRepository;

  public ListLectureInvitesUseCase(
    LectureRepository lectureRepository,
    LectureInviteRepository lectureInviteRepository
  ) {
    this.lectureRepository = lectureRepository;
    this.lectureInviteRepository = lectureInviteRepository;
  }

  public List<LectureInvite> execute(String lectureId) {
    LectureId resolvedLectureId = new LectureId(lectureId);
    this.lectureRepository
      .findById(resolvedLectureId)
      .orElseThrow(() ->
        new ApiException(HttpStatus.NOT_FOUND, "LECTURE_NOT_FOUND", "Lecture not found")
      );
    return this.lectureInviteRepository.findByLectureId(resolvedLectureId);
  }
}
