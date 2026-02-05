package com.livequiz.backend.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLectureRepository extends JpaRepository<LectureEntity, String> {}
