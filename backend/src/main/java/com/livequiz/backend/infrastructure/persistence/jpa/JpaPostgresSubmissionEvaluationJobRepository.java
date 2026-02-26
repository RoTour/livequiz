package com.livequiz.backend.infrastructure.persistence.jpa;

import com.livequiz.backend.application.messaging.SubmissionEvaluationJob;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobRepository;
import com.livequiz.backend.application.messaging.SubmissionEvaluationJobStatus;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JpaPostgresSubmissionEvaluationJobRepository
  implements SubmissionEvaluationJobRepository {

  private final JpaSubmissionEvaluationJobRepository jpaSubmissionEvaluationJobRepository;

  public JpaPostgresSubmissionEvaluationJobRepository(
    JpaSubmissionEvaluationJobRepository jpaSubmissionEvaluationJobRepository
  ) {
    this.jpaSubmissionEvaluationJobRepository = jpaSubmissionEvaluationJobRepository;
  }

  @Override
  public void save(SubmissionEvaluationJob submissionEvaluationJob) {
    this.jpaSubmissionEvaluationJobRepository.save(toEntity(submissionEvaluationJob));
  }

  @Override
  public Optional<SubmissionEvaluationJob> findBySubmissionId(String submissionId) {
    return this.jpaSubmissionEvaluationJobRepository.findById(submissionId).map(this::toDomain);
  }

  @Override
  public boolean claimForProcessing(String submissionId, Instant now) {
    return this.jpaSubmissionEvaluationJobRepository.claimForProcessing(submissionId, now) > 0;
  }

  private SubmissionEvaluationJobEntity toEntity(SubmissionEvaluationJob submissionEvaluationJob) {
    return new SubmissionEvaluationJobEntity(
      submissionEvaluationJob.submissionId(),
      submissionEvaluationJob.lectureId(),
      submissionEvaluationJob.questionId(),
      submissionEvaluationJob.studentId(),
      submissionEvaluationJob.status().name(),
      submissionEvaluationJob.attemptCount(),
      submissionEvaluationJob.nextAttemptAt(),
      submissionEvaluationJob.lastError(),
      submissionEvaluationJob.createdAt(),
      submissionEvaluationJob.updatedAt()
    );
  }

  private SubmissionEvaluationJob toDomain(SubmissionEvaluationJobEntity entity) {
    return new SubmissionEvaluationJob(
      entity.getSubmissionId(),
      entity.getLectureId(),
      entity.getQuestionId(),
      entity.getStudentId(),
      SubmissionEvaluationJobStatus.valueOf(entity.getStatus()),
      entity.getAttemptCount(),
      entity.getNextAttemptAt(),
      entity.getLastError(),
      entity.getCreatedAt(),
      entity.getUpdatedAt()
    );
  }
}
