package com.livequiz.backend.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    String inviteCode = createInvite(instructorToken, lectureId);
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
      .andExpect(status().isOk());

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

  private String createInvite(String instructorToken, String lectureId) throws Exception {
    String response = this.mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    int keyIndex = response.indexOf("joinCode");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
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

  private String extractField(String response, String fieldName) {
    int keyIndex = response.indexOf("\"" + fieldName + "\"");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
  }
}
