package com.livequiz.backend.infrastructure.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.livequiz.backend.domain.lecture.LectureInvite;
import com.livequiz.backend.domain.lecture.LectureInviteRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
class StudentFlowIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LectureInviteRepository lectureInviteRepository;

  @Test
  void should_join_with_invite_and_submit_with_cooldown() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String questionId = addQuestion(
      instructorToken,
      lectureId,
      "What is consensus?",
      "Agreement on a value",
      60
    );

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions/unlock-next", lectureId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk());

    String inviteResponse = createInviteResponse(instructorToken, lectureId);
    String inviteCode = extractField(inviteResponse, "joinCode");
    String joinUrl = extractField(inviteResponse, "joinUrl");
    org.junit.jupiter.api.Assertions.assertTrue(joinUrl.contains("/student/join/"));
    org.junit.jupiter.api.Assertions.assertFalse(joinUrl.contains("?token="));
    String studentToken = login("student", "password");

    String firstJoinResponse = mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"code\":\"" + inviteCode + "\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.alreadyEnrolled").value(false))
      .andReturn()
      .getResponse()
      .getContentAsString();

    String secondJoinResponse = mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"code\":\"" + inviteCode + "\"}")
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.alreadyEnrolled").value(true))
      .andReturn()
      .getResponse()
      .getContentAsString();

    String firstEnrolledAt = extractField(firstJoinResponse, "enrolledAt");
    String secondEnrolledAt = extractField(secondJoinResponse, "enrolledAt");
    org.junit.jupiter.api.Assertions.assertEquals(
      firstEnrolledAt,
      secondEnrolledAt
    );

    mockMvc
      .perform(
        get("/api/lectures/{lectureId}/students/me/next-question", lectureId)
          .header("Authorization", "Bearer " + studentToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.hasQuestion").value(true))
      .andExpect(jsonPath("$.questionId").value(questionId));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", lectureId)
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"" +
            questionId +
            "\",\"answerText\":\"Consensus is agreement\"}"
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.answerStatus").exists());

    mockMvc
      .perform(
        get("/api/lectures/{lectureId}/students/me/answer-statuses", lectureId)
          .header("Authorization", "Bearer " + studentToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].questionId").value(questionId))
      .andExpect(jsonPath("$[0].status").exists());

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", lectureId)
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"" +
            questionId +
            "\",\"answerText\":\"Second attempt\"}"
          )
      )
      .andExpect(status().isTooManyRequests())
      .andExpect(jsonPath("$.code").value("SUBMISSION_COOLDOWN"))
      .andExpect(jsonPath("$.details.retryAfterSeconds").exists());

    mockMvc
      .perform(
        get("/api/lectures/{lectureId}/students/me/next-question", lectureId)
          .header("Authorization", "Bearer " + studentToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.hasQuestion").value(false));
  }

  @Test
  void should_list_only_joined_lectures_for_current_student() throws Exception {
    String instructorToken = login("instructor", "password");
    String studentToken = login("student", "password");

    String answeredLectureId = createLecture(instructorToken, "Answered Lecture");
    String pendingLectureId = createLecture(instructorToken, "Pending Lecture");
    String notJoinedLectureId = createLecture(instructorToken, "Not Joined Lecture");

    String answeredQuestionId = addQuestion(
      instructorToken,
      answeredLectureId,
      "What is eventual consistency?",
      "Consistency model",
      60
    );

    String answeredLectureInviteCode = extractField(
      createInviteResponse(instructorToken, answeredLectureId),
      "joinCode"
    );
    String pendingLectureInviteCode = extractField(
      createInviteResponse(instructorToken, pendingLectureId),
      "joinCode"
    );

    joinLectureByCode(studentToken, answeredLectureInviteCode);
    joinLectureByCode(studentToken, pendingLectureInviteCode);

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions/unlock-next", answeredLectureId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk());

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", answeredLectureId)
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"" +
            answeredQuestionId +
            "\",\"answerText\":\"Consistency over time\"}"
          )
      )
      .andExpect(status().isOk());

    mockMvc
      .perform(get("/api/lectures/students/me").header("Authorization", "Bearer " + studentToken))
      .andExpect(status().isOk())
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + answeredLectureId + "')].title").value(
          hasItem("Answered Lecture")
        )
      )
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + answeredLectureId + "')].questionCount").value(
          hasItem(1)
        )
      )
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + answeredLectureId + "')].answeredCount").value(
          hasItem(1)
        )
      )
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + pendingLectureId + "')].title").value(
          hasItem("Pending Lecture")
        )
      )
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + pendingLectureId + "')].questionCount").value(
          hasItem(0)
        )
      )
      .andExpect(
        jsonPath("$[?(@.lectureId=='" + pendingLectureId + "')].answeredCount").value(
          hasItem(0)
        )
      )
      .andExpect(jsonPath("$[?(@.lectureId=='" + notJoinedLectureId + "')]" ).isEmpty());
  }

  @Test
  void should_reject_revoke_when_lecture_path_does_not_match_invite_lecture() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureA = createLecture(instructorToken, "Lecture A");
    String lectureB = createLecture(instructorToken, "Lecture B");
    String inviteId = createInviteAndGetInviteId(instructorToken, lectureA);

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites/{inviteId}/revoke", lectureB, inviteId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("INVITE_NOT_FOUND"));
  }

  @Test
  void should_require_invite_credentials_when_joining() throws Exception {
    String studentToken = login("student", "password");

    mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{}")
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVITE_CREDENTIALS_REQUIRED"));
  }

  @Test
  void should_return_explicit_revoked_error_when_joining_non_active_invite() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String inviteResponse = createInviteResponse(instructorToken, lectureId);
    String inviteId = extractField(inviteResponse, "inviteId");
    String joinCode = extractField(inviteResponse, "joinCode");
    String studentToken = login("student", "password");

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites/{inviteId}/revoke", lectureId, inviteId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk());

    mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"code\":\"" + joinCode + "\"}")
      )
      .andExpect(status().isGone())
      .andExpect(jsonPath("$.code").value("INVITE_REVOKED"));
  }

  @Test
  void should_return_explicit_expired_error_when_joining_non_active_invite() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String inviteResponse = createInviteResponse(instructorToken, lectureId);
    String inviteId = extractField(inviteResponse, "inviteId");
    String joinCode = extractField(inviteResponse, "joinCode");
    String studentToken = login("student", "password");

    expireInvite(inviteId);

    mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"code\":\"" + joinCode + "\"}")
      )
      .andExpect(status().isGone())
      .andExpect(jsonPath("$.code").value("INVITE_EXPIRED"));
  }

  @Test
  void should_keep_join_idempotent_after_invite_revocation_for_enrolled_student() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String inviteResponse = createInviteResponse(instructorToken, lectureId);
    String inviteId = extractField(inviteResponse, "inviteId");
    String joinCode = extractField(inviteResponse, "joinCode");
    String studentToken = login("student", "password");

    String firstJoinResponse = joinLectureByCode(studentToken, joinCode);

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites/{inviteId}/revoke", lectureId, inviteId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk());

    String secondJoinResponse = joinLectureByCode(studentToken, joinCode);

    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(firstJoinResponse, "alreadyEnrolled"),
      "false"
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(secondJoinResponse, "alreadyEnrolled"),
      "true"
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(firstJoinResponse, "enrolledAt"),
      extractField(secondJoinResponse, "enrolledAt")
    );
  }

  @Test
  void should_require_enrollment_before_next_question_and_submission() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String questionId = addQuestion(
      instructorToken,
      lectureId,
      "What is consensus?",
      "Agreement on a value",
      60
    );
    String studentToken = login("student", "password");

    mockMvc
      .perform(
        get("/api/lectures/{lectureId}/students/me/next-question", lectureId)
          .header("Authorization", "Bearer " + studentToken)
      )
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value("LECTURE_ENROLLMENT_REQUIRED"));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", lectureId)
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"" +
            questionId +
            "\",\"answerText\":\"Consensus is agreement\"}"
          )
      )
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value("LECTURE_ENROLLMENT_REQUIRED"));
  }

  @Test
  void should_reject_submission_for_locked_or_unknown_question() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String questionId = addQuestion(
      instructorToken,
      lectureId,
      "What is consensus?",
      "Agreement on a value",
      60
    );
    String inviteCode = extractField(createInviteResponse(instructorToken, lectureId), "joinCode");
    String studentToken = login("student", "password");
    joinLectureByCode(studentToken, inviteCode);

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", lectureId)
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"" +
            questionId +
            "\",\"answerText\":\"Consensus is agreement\"}"
          )
      )
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value("QUESTION_LOCKED"));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/submissions", lectureId)
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content(
            "{\"questionId\":\"unknown-question\",\"answerText\":\"Consensus is agreement\"}"
          )
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("QUESTION_NOT_FOUND"));
  }

  @Test
  void should_join_with_invite_token_idempotently() throws Exception {
    String instructorToken = login("instructor", "password");
    String lectureId = createLecture(instructorToken, "Distributed Systems");
    String inviteResponse = createInviteResponse(instructorToken, lectureId);
    String token = extractInviteToken(extractField(inviteResponse, "joinUrl"));
    String studentToken = login("student", "password");

    String firstJoinResponse = joinLectureByToken(studentToken, token);
    String secondJoinResponse = joinLectureByToken(studentToken, token);

    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(firstJoinResponse, "lectureId"),
      lectureId
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(firstJoinResponse, "studentId"),
      "student"
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(firstJoinResponse, "alreadyEnrolled"),
      "false"
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(secondJoinResponse, "alreadyEnrolled"),
      "true"
    );
    org.junit.jupiter.api.Assertions.assertEquals(
      extractField(firstJoinResponse, "enrolledAt"),
      extractField(secondJoinResponse, "enrolledAt")
    );
  }

  private String login(String username, String password) throws Exception {
    String response = this.mockMvc
      .perform(
        post("/api/auth/login")
          .contentType("application/json")
          .content(
            "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"
          )
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    int start = response.indexOf(":\"") + 2;
    int end = response.lastIndexOf('"');
    return response.substring(start, end);
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
    int start = response.indexOf(":\"") + 2;
    int end = response.lastIndexOf('"');
    return response.substring(start, end);
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
    int keyIndex = response.indexOf("questionId");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
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

  private String createInviteAndGetInviteId(String instructorToken, String lectureId) throws Exception {
    String response = this.mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    int keyIndex = response.indexOf("inviteId");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
  }

  private String joinLectureByCode(String studentToken, String inviteCode) throws Exception {
    return mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"code\":\"" + inviteCode + "\"}")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String joinLectureByToken(String studentToken, String token) throws Exception {
    return mockMvc
      .perform(
        post("/api/lectures/join")
          .header("Authorization", "Bearer " + studentToken)
          .contentType("application/json")
          .content("{\"token\":\"" + token + "\"}")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String extractInviteToken(String joinUrl) {
    int tokenStart = joinUrl.lastIndexOf('/') + 1;
    return joinUrl.substring(tokenStart);
  }

  private void expireInvite(String inviteId) {
    LectureInvite invite = this.lectureInviteRepository.findByInviteId(inviteId).orElseThrow();
    Instant createdAt = Instant.now().minus(Duration.ofHours(2));
    LectureInvite expiredInvite = new LectureInvite(
      invite.inviteId(),
      invite.lectureId(),
      invite.createdByInstructorId(),
      invite.joinCode(),
      invite.tokenHash(),
      createdAt,
      createdAt.plus(Duration.ofMinutes(30)),
      null
    );
    this.lectureInviteRepository.save(expiredInvite);
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
}
