package com.livequiz.backend.application;

import com.livequiz.backend.domain.student.StudentIdentity;
import com.livequiz.backend.domain.student.StudentIdentityRepository;
import com.livequiz.backend.infrastructure.web.jwt.JwtService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IssueAnonymousStudentTokenUseCase {

  public record AnonymousAuthResult(String token, String studentId) {}

  private final StudentIdentityRepository studentIdentityRepository;
  private final JwtService jwtService;

  public IssueAnonymousStudentTokenUseCase(
    StudentIdentityRepository studentIdentityRepository,
    JwtService jwtService
  ) {
    this.studentIdentityRepository = studentIdentityRepository;
    this.jwtService = jwtService;
  }

  public AnonymousAuthResult execute() {
    Instant now = Instant.now();
    String studentId = UUID.randomUUID().toString();
    StudentIdentity anonymousIdentity = StudentIdentity.anonymous(studentId, now);
    this.studentIdentityRepository.save(anonymousIdentity);
    String token = this.jwtService.createStudentToken(studentId, true, false);
    return new AnonymousAuthResult(token, studentId);
  }
}
