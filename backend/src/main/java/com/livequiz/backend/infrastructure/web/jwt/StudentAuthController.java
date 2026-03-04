package com.livequiz.backend.infrastructure.web.jwt;

import com.livequiz.backend.application.CurrentUserService;
import com.livequiz.backend.application.IssueAnonymousStudentTokenUseCase;
import com.livequiz.backend.application.RegisterStudentEmailUseCase;
import com.livequiz.backend.application.RequestStudentMagicLoginUseCase;
import com.livequiz.backend.application.ResendStudentVerificationUseCase;
import com.livequiz.backend.application.VerifyStudentEmailUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class StudentAuthController {

  public record TokenResponse(String token) {}

  public record GenericAuthResponse(String status) {}

  public record RegisterEmailRequest(String email) {}

  public record ResendVerificationRequest(String email) {}

  public record RequestStudentLoginRequest(String email) {}

  public record VerifyEmailRequest(String token) {}

  private final IssueAnonymousStudentTokenUseCase issueAnonymousStudentTokenUseCase;
  private final RegisterStudentEmailUseCase registerStudentEmailUseCase;
  private final RequestStudentMagicLoginUseCase requestStudentMagicLoginUseCase;
  private final ResendStudentVerificationUseCase resendStudentVerificationUseCase;
  private final VerifyStudentEmailUseCase verifyStudentEmailUseCase;
  private final CurrentUserService currentUserService;

  public StudentAuthController(
    IssueAnonymousStudentTokenUseCase issueAnonymousStudentTokenUseCase,
    RegisterStudentEmailUseCase registerStudentEmailUseCase,
    RequestStudentMagicLoginUseCase requestStudentMagicLoginUseCase,
    ResendStudentVerificationUseCase resendStudentVerificationUseCase,
    VerifyStudentEmailUseCase verifyStudentEmailUseCase,
    CurrentUserService currentUserService
  ) {
    this.issueAnonymousStudentTokenUseCase = issueAnonymousStudentTokenUseCase;
    this.registerStudentEmailUseCase = registerStudentEmailUseCase;
    this.requestStudentMagicLoginUseCase = requestStudentMagicLoginUseCase;
    this.resendStudentVerificationUseCase = resendStudentVerificationUseCase;
    this.verifyStudentEmailUseCase = verifyStudentEmailUseCase;
    this.currentUserService = currentUserService;
  }

  @PostMapping("/anonymous")
  public TokenResponse issueAnonymousToken() {
    IssueAnonymousStudentTokenUseCase.AnonymousAuthResult result = this.issueAnonymousStudentTokenUseCase.execute();
    return new TokenResponse(result.token());
  }

  @PostMapping("/students/request-login")
  public GenericAuthResponse requestStudentLogin(
    @RequestBody(required = false) RequestStudentLoginRequest request
  ) {
    String email = request == null ? null : request.email();
    RequestStudentMagicLoginUseCase.RequestLoginResult result = this.requestStudentMagicLoginUseCase.execute(
        email
      );
    return new GenericAuthResponse(result.status());
  }

  @PostMapping("/students/register-email")
  public GenericAuthResponse registerEmail(@RequestBody RegisterEmailRequest request) {
    String studentId = this.currentUserService.requireUserId();
    RegisterStudentEmailUseCase.RegisterEmailResult result = this.registerStudentEmailUseCase.execute(
        studentId,
        request.email()
      );
    return new GenericAuthResponse(result.status());
  }

  @PostMapping("/students/resend-verification")
  public GenericAuthResponse resendVerification(
    @RequestBody(required = false) ResendVerificationRequest request
  ) {
    String studentId = this.currentUserService.requireUserId();
    String email = request == null ? null : request.email();
    ResendStudentVerificationUseCase.ResendResult result = this.resendStudentVerificationUseCase.execute(
        studentId,
        email
      );
    return new GenericAuthResponse(result.status());
  }

  @PostMapping("/students/verify-email")
  public TokenResponse verifyEmail(@RequestBody VerifyEmailRequest request) {
    VerifyStudentEmailUseCase.VerifyResult result = this.verifyStudentEmailUseCase.execute(
        request.token()
      );
    return new TokenResponse(result.token());
  }
}
