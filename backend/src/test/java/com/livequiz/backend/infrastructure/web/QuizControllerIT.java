package com.livequiz.backend.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.livequiz.backend.domain.lecture.LectureId;
import com.livequiz.backend.domain.lecture.LectureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
public class QuizControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LectureRepository lectureRepository;

  private String loginAsInstructor() throws Exception {
    String requestBody = """
      {
        "username": "instructor",
        "password": "password"
      }
      """;

    String response = mockMvc
      .perform(
        post("/api/auth/login").contentType("application/json").content(requestBody)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    int start = response.indexOf(":\"") + 2;
    int end = response.lastIndexOf('"');
    return response.substring(start, end);
  }

  private String loginAsStudent() throws Exception {
    String requestBody = """
      {
        "username": "student",
        "password": "password"
      }
      """;

    String response = mockMvc
      .perform(
        post("/api/auth/login").contentType("application/json").content(requestBody)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    int start = response.indexOf(":\"") + 2;
    int end = response.lastIndexOf('"');
    return response.substring(start, end);
  }

  @Test
  void should_return_bad_request_when_lecture_title_is_empty() throws Exception {
    String token = loginAsInstructor();
    String requestBody = """
      {
        "title": ""
      }
      """;

    mockMvc
        .perform(
          post("/api/lectures")
          .contentType("application/json")
          .header("Authorization", "Bearer " + token)
          .content(requestBody)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("DOMAIN_ERROR"))
      .andExpect(jsonPath("$.message").value("Title cannot be null or blank"));
  }

  @Test
  void should_return_not_found_for_invalid_endpoint() throws Exception {
    String token = loginAsInstructor();
    mockMvc
      .perform(get("/api/invalid-endpoint").header("Authorization", "Bearer " + token))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("NOT_FOUND"))
      .andExpect(
        jsonPath("$.message").value("The requested resource was not found")
      );
  }

  @Test
  void should_keep_healthcheck_public_without_authentication() throws Exception {
    mockMvc
      .perform(get("/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("OK"));

    mockMvc
      .perform(get("/api/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("OK"));
  }

  @Test
  void should_reject_student_for_instructor_only_endpoints() throws Exception {
    String instructorToken = loginAsInstructor();
    String studentToken = loginAsStudent();
    String createLectureRequestBody = """
      {
        "title": "Domain Model"
      }
      """;

    String createLectureResponse = mockMvc
      .perform(
        post("/api/lectures")
          .contentType("application/json")
          .header("Authorization", "Bearer " + instructorToken)
          .content(createLectureRequestBody)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    String lectureId = extractField(createLectureResponse, "lectureId");

    mockMvc
      .perform(
        post("/api/lectures")
          .contentType("application/json")
          .header("Authorization", "Bearer " + studentToken)
          .content(createLectureRequestBody)
      )
      .andExpect(status().isForbidden());

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions/unlock-next", lectureId)
          .header("Authorization", "Bearer " + studentToken)
      )
      .andExpect(status().isForbidden());

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + studentToken)
      )
      .andExpect(status().isForbidden());
  }

  @Test
  void should_require_authentication_for_protected_endpoints() throws Exception {
    String requestBody = """
      {
        "title": "Domain Model"
      }
      """;

    mockMvc
      .perform(post("/api/lectures").contentType("application/json").content(requestBody))
      .andExpect(status().isForbidden());

    mockMvc
      .perform(get("/api/lectures/lecture-1/state"))
      .andExpect(status().isForbidden());
  }

  @Test
  void should_store_lecture_ownership_metadata_on_create() throws Exception {
    String instructorToken = loginAsInstructor();
    String requestBody = """
      {
        "title": "Ownership Metadata"
      }
      """;

    String createLectureResponse = mockMvc
      .perform(
        post("/api/lectures")
          .contentType("application/json")
          .header("Authorization", "Bearer " + instructorToken)
          .content(requestBody)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    String lectureId = extractField(createLectureResponse, "lectureId");
    var persistedLecture = lectureRepository.findById(new LectureId(lectureId)).orElseThrow();

    assertEquals("instructor", persistedLecture.createdByInstructorId());
    assertNotNull(persistedLecture.createdAt());
  }

  private String extractField(String response, String fieldName) {
    int keyIndex = response.indexOf("\"" + fieldName + "\"");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
  }
}
