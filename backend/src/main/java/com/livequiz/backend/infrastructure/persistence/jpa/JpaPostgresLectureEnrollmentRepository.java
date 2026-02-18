package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.domain.lecture.LectureEnrollment;
import com.livequiz.backend.domain.lecture.LectureEnrollmentRepository;
import com.livequiz.backend.domain.lecture.LectureId;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresLectureEnrollmentRepository
  implements LectureEnrollmentRepository {

  private final JpaLectureEnrollmentRepository jpaLectureEnrollmentRepository;

  public JpaPostgresLectureEnrollmentRepository(
    JpaLectureEnrollmentRepository jpaLectureEnrollmentRepository
  ) {
    this.jpaLectureEnrollmentRepository = jpaLectureEnrollmentRepository;
  }

  @Override
  public void save(LectureEnrollment enrollment) {
    this.jpaLectureEnrollmentRepository.save(
        new LectureEnrollmentEntity(
          new LectureEnrollmentId(enrollment.lectureId().value(), enrollment.studentId()),
          enrollment.enrolledAt()
        )
      );
  }

  @Override
  public boolean existsByLectureIdAndStudentId(LectureId lectureId, String studentId) {
    return this.jpaLectureEnrollmentRepository.existsById(
        new LectureEnrollmentId(lectureId.value(), studentId)
      );
  }

  @Override
  public Optional<LectureEnrollment> findByLectureIdAndStudentId(
    LectureId lectureId,
    String studentId
  ) {
    return this.jpaLectureEnrollmentRepository
      .findById(new LectureEnrollmentId(lectureId.value(), studentId))
      .map(entity ->
        new LectureEnrollment(
          new LectureId(entity.getId().getLectureId()),
          entity.getId().getStudentId(),
          entity.getEnrolledAt()
        )
      );
  }
}
