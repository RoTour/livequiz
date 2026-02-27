package com.livequiz.backend.infrastructure.web;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.livequiz.backend.application.StudentVerificationEmailSender;
import com.livequiz.backend.application.StudentVerificationTokenService;
import com.livequiz.backend.domain.student.EmailVerificationChallenge;
import com.livequiz.backend.domain.student.EmailVerificationChallengeRepository;
import com.livequiz.backend.infrastructure.web.jwt.JwtService;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
@Import(StudentEmailAuthIT.CapturingEmailConfig.class)
@TestPropertySource(properties = "livequiz.teacher-role-classification-enabled=true")
class StudentEmailAuthIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private CapturingStudentVerificationEmailSender capturingStudentVerificationEmailSender;

  @Autowired
  private EmailVerificationChallengeRepository emailVerificationChallengeRepository;

  @Autowired
  private StudentVerificationTokenService studentVerificationTokenService;

  @Test
  void should_issue_anonymous_student_token_with_expected_claims() throws Exception {
    String anonymousToken = issueAnonymousToken();

    JwtService.TokenClaims tokenClaims = this.jwtService.validateToken(anonymousToken);
    assertNotNull(tokenClaims);
    assertEquals("STUDENT", tokenClaims.role());
    assertEquals(true, tokenClaims.anonymous());
    assertEquals(false, tokenClaims.emailVerified());
    org.junit.jupiter.api.Assertions.assertFalse(tokenClaims.subject().isBlank());
  }

  @Test
  void should_classify_login_role_from_teacher_registry() throws Exception {
    String instructorToken = login("instructor@ynov.com", "password");
    String studentToken = login("student", "password");
    String instructorCandidateToken = login("instructor-candidate@ynov.com", "password");

    JwtService.TokenClaims instructorClaims = this.jwtService.validateToken(instructorToken);
    JwtService.TokenClaims studentClaims = this.jwtService.validateToken(studentToken);
    JwtService.TokenClaims instructorCandidateClaims = this.jwtService.validateToken(
        instructorCandidateToken
      );

    assertNotNull(instructorClaims);
    assertNotNull(studentClaims);
    assertNotNull(instructorCandidateClaims);
    assertEquals("INSTRUCTOR", instructorClaims.role());
    assertEquals("STUDENT", studentClaims.role());
    assertEquals("STUDENT", instructorCandidateClaims.role());
  }

  @Test
  void should_require_student_authentication_for_register_and_resend() throws Exception {
    this.mockMvc
      .perform(
        post("/api/auth/students/register-email")
          .contentType("application/json")
          .content("{\"email\":\"student@ynov.com\"}")
      )
      .andExpect(status().isForbidden());

    this.mockMvc
      .perform(
        post("/api/auth/students/resend-verification")
          .contentType("application/json")
          .content("{}")
      )
      .andExpect(status().isForbidden());
  }

  @Test
  void should_reject_non_ynov_domain_for_student_registration() throws Exception {
    String anonymousToken = issueAnonymousToken();

    this.mockMvc
      .perform(
        post("/api/auth/students/register-email")
          .header("Authorization", "Bearer " + anonymousToken)
          .contentType("application/json")
          .content("{\"email\":\"student@gmail.com\"}")
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("EMAIL_DOMAIN_NOT_ALLOWED"));
  }

  @Test
  void should_keep_enrollment_and_submission_continuity_after_email_verification() throws Exception {
    String instructorToken = login("instructor@ynov.com", "password");
    String lectureId = createLecture(instructorToken, "Identity Continuity Lecture");
    String questionId = addQuestion(
      instructorToken,
      lectureId,
      "What is continuity?",
      "Stable identity",
      45
    );

    this.mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions/unlock-next", lectureId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk());

    String joinCode = extractField(createInviteResponse(instructorToken, lectureId), "joinCode");
    String anonymousToken = issueAnonymousToken();
    String initialStudentId = this.jwtService.validateToken(anonymousToken).subject();

    this.mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + anonymousToken)
          .contentType("application/json")
          .content("{\"code\":\"" + joinCode + "\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.alreadyEnrolled").value(false));

    this.mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", lectureId)
          .header("Authorization", "Bearer " + anonymousToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"" +
            questionId +
            "\",\"answerText\":\"Identity survives verification\"}"
          )
      )
      .andExpect(status().isOk());

    this.mockMvc
      .perform(
        post("/api/auth/students/register-email")
          .header("Authorization", "Bearer " + anonymousToken)
          .contentType("application/json")
          .content("{\"email\":\"  Student@Ynov.Com \"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("VERIFICATION_EMAIL_SENT_IF_ALLOWED"));

    String verificationToken = this.capturingStudentVerificationEmailSender.tokenForEmail(
        "student@ynov.com"
      );
    assertNotNull(verificationToken);

    String verifyResponse = this.mockMvc
      .perform(
        post("/api/auth/students/verify-email")
          .contentType("application/json")
          .content("{\"token\":\"" + verificationToken + "\"}")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    String verifiedToken = extractField(verifyResponse, "token");
    JwtService.TokenClaims verifiedClaims = this.jwtService.validateToken(verifiedToken);
    assertNotNull(verifiedClaims);
    assertEquals(initialStudentId, verifiedClaims.subject());
    assertEquals("STUDENT", verifiedClaims.role());
    assertFalse(verifiedClaims.anonymous());
    assertEquals(true, verifiedClaims.emailVerified());

    this.mockMvc
      .perform(
        get("/api/lectures/students/me")
          .header("Authorization", "Bearer " + verifiedToken)
      )
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + lectureId + "')].title").value(
          hasItem("Identity Continuity Lecture")
        )
      );

    this.mockMvc
      .perform(
        get("/api/lectures/{lectureId}/students/me/next-question", lectureId)
          .header("Authorization", "Bearer " + verifiedToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.hasQuestion").value(false));
  }

  @Test
  void should_reject_consumed_verification_token() throws Exception {
    String anonymousToken = issueAnonymousToken();
    String verificationToken = registerEmailAndCaptureToken(
      anonymousToken,
      "consumed@ynov.com"
    );

    this.mockMvc
      .perform(
        post("/api/auth/students/verify-email")
          .contentType("application/json")
          .content("{\"token\":\"" + verificationToken + "\"}")
      )
      .andExpect(status().isOk());

    this.mockMvc
      .perform(
        post("/api/auth/students/verify-email")
          .contentType("application/json")
          .content("{\"token\":\"" + verificationToken + "\"}")
      )
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_TOKEN_CONSUMED"));
  }

  @Test
  void should_reject_expired_verification_token() throws Exception {
    String anonymousToken = issueAnonymousToken();
    String verificationToken = registerEmailAndCaptureToken(
      anonymousToken,
      "expired@ynov.com"
    );
    String tokenHash = this.studentVerificationTokenService.hashToken(verificationToken);
    EmailVerificationChallenge challenge = this.emailVerificationChallengeRepository
      .findLatestByTokenHash(tokenHash)
      .orElseThrow();

    EmailVerificationChallenge expiredChallenge = new EmailVerificationChallenge(
      challenge.challengeId(),
      challenge.studentId(),
      challenge.email(),
      challenge.tokenHash(),
      challenge.createdAt().minusSeconds(60),
      challenge.consumedAt(),
      challenge.createdAt().minusSeconds(120)
    );
    this.emailVerificationChallengeRepository.save(expiredChallenge);

    this.mockMvc
      .perform(
        post("/api/auth/students/verify-email")
          .contentType("application/json")
          .content("{\"token\":\"" + verificationToken + "\"}")
      )
      .andExpect(status().isGone())
      .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_TOKEN_EXPIRED"));
  }

  private String registerEmailAndCaptureToken(String studentToken, String email) throws Exception {
    this.mockMvc
      .perform(
        post("/api/auth/students/register-email")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"email\":\"" + email + "\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("VERIFICATION_EMAIL_SENT_IF_ALLOWED"));

    String token = this.capturingStudentVerificationEmailSender.tokenForEmail(email);
    assertNotNull(token);
    return token;
  }

  private String issueAnonymousToken() throws Exception {
    String response = this.mockMvc
      .perform(post("/api/auth/anonymous").contentType("application/json"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return extractField(response, "token");
  }

  private String login(String identifier, String password) throws Exception {
    String payload = identifier.contains("@")
      ? "{\"email\":\"" + identifier + "\",\"password\":\"" + password + "\"}"
      : "{\"username\":\"" + identifier + "\",\"password\":\"" + password + "\"}";
    String response = this.mockMvc
      .perform(
        post("/api/auth/login")
          .contentType("application/json")
          .content(payload)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return extractField(response, "token");
  }

  private String createLecture(String instructorToken, String title) throws Exception {
    String response = this.mockMvc
      .perform(
        post("/api/lectures")
          .header("Authorization", "Bearer " + instructorToken)
          .contentType("application/json")
          .content("{\"title\":\"" + title + "\"}")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return extractField(response, "lectureId");
  }

  private String addQuestion(
    String instructorToken,
    String lectureId,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds
  ) throws Exception {
    String response = this.mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions", lectureId)
          .header("Authorization", "Bearer " + instructorToken)
          .contentType("application/json")
          .content(
            "{\"prompt\":\"" +
            prompt +
            "\",\"modelAnswer\":\"" +
            modelAnswer +
            "\",\"timeLimitSeconds\":" +
            timeLimitSeconds +
            "}"
          )
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    return extractField(response, "questionId");
  }

  private String createInviteResponse(String instructorToken, String lectureId) throws Exception {
    return this.mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String extractField(String response, String fieldName) {
    int keyIndex = response.indexOf("\"" + fieldName + "\"");
    int valueStart = response.indexOf(':', keyIndex) + 1;
    while (Character.isWhitespace(response.charAt(valueStart))) {
      valueStart++;
    }

    if (response.charAt(valueStart) == '"') {
      int start = valueStart + 1;
      int end = response.indexOf('"', start);
      return response.substring(start, end);
    }

    int end = response.indexOf(',', valueStart);
    if (end == -1) {
      end = response.indexOf('}', valueStart);
    }
    return response.substring(valueStart, end).trim();
  }

  @TestConfiguration
  static class CapturingEmailConfig {

    @Bean
    @Primary
    CapturingStudentVerificationEmailSender capturingStudentVerificationEmailSender() {
      return new CapturingStudentVerificationEmailSender();
    }
  }

  static class CapturingStudentVerificationEmailSender
    implements StudentVerificationEmailSender {

    private final Map<String, String> tokenByEmail = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(
      String email,
      String token,
      String verificationUrl,
      Instant expiresAt
    ) {
      this.tokenByEmail.put(normalize(email), token);
    }

    String tokenForEmail(String email) {
      return this.tokenByEmail.get(normalize(email));
    }

    private String normalize(String email) {
      return email.trim().toLowerCase(Locale.ROOT);
    }
  }
}
