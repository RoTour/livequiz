package com.livequiz.backend.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.hasItem;

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

  private String loginAsSecondInstructor() throws Exception {
    String requestBody = """
      {
        "username": "instructor2",
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

    mockMvc
      .perform(get("/api/lectures").header("Authorization", "Bearer " + studentToken))
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

    mockMvc
      .perform(get("/api/lectures"))
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

  @Test
  void should_list_instructor_lectures_with_summary_fields() throws Exception {
    String instructorToken = loginAsInstructor();
    String lectureOneId = createLecture(instructorToken, "First Lecture");
    String lectureTwoId = createLecture(instructorToken, "Second Lecture");
    addQuestion(instructorToken, lectureOneId, "Q1", "A1", 60);
    addQuestion(instructorToken, lectureOneId, "Q2", "A2", 60);

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions/unlock-next", lectureOneId)
          .header("Authorization", "Bearer " + instructorToken)
      )
      .andExpect(status().isOk());

    mockMvc
      .perform(get("/api/lectures").header("Authorization", "Bearer " + instructorToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureOneId + "')].title").value(hasItem("First Lecture")))
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureOneId + "')].questionCount").value(hasItem(2)))
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureOneId + "')].unlockedCount").value(hasItem(1)))
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureOneId + "')].createdAt").isNotEmpty())
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureTwoId + "')].title").value(hasItem("Second Lecture")))
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureTwoId + "')].questionCount").value(hasItem(0)))
      .andExpect(jsonPath("$[?(@.lectureId=='" + lectureTwoId + "')].unlockedCount").value(hasItem(0)));
  }

  @Test
  void should_enforce_instructor_ownership_on_lecture_resources() throws Exception {
    String ownerToken = loginAsInstructor();
    String otherInstructorToken = loginAsSecondInstructor();
    String lectureId = createLecture(ownerToken, "Owner Lecture");
    String inviteId = createInvite(ownerToken, lectureId);
    String addQuestionRequestBody = """
      {
        "prompt": "Ownership check question",
        "modelAnswer": "Ownership check answer",
        "timeLimitSeconds": 60
      }
      """;

    mockMvc
      .perform(
        get("/api/lectures/{lectureId}/state", lectureId)
          .header("Authorization", "Bearer " + otherInstructorToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("LECTURE_NOT_FOUND"));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions", lectureId)
          .header("Authorization", "Bearer " + otherInstructorToken)
          .contentType("application/json")
          .content(addQuestionRequestBody)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("LECTURE_NOT_FOUND"));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions/unlock-next", lectureId)
          .header("Authorization", "Bearer " + otherInstructorToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("LECTURE_NOT_FOUND"));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + otherInstructorToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("LECTURE_NOT_FOUND"));

    mockMvc
      .perform(
        get("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + otherInstructorToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("LECTURE_NOT_FOUND"));

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites/{inviteId}/revoke", lectureId, inviteId)
          .header("Authorization", "Bearer " + otherInstructorToken)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("LECTURE_NOT_FOUND"));
  }

  @Test
  void should_list_only_owned_lectures_per_instructor() throws Exception {
    String ownerToken = loginAsInstructor();
    String otherInstructorToken = loginAsSecondInstructor();
    String ownerLectureId = createLecture(ownerToken, "Owner Lecture");
    String otherLectureId = createLecture(otherInstructorToken, "Other Lecture");

    mockMvc
      .perform(get("/api/lectures").header("Authorization", "Bearer " + ownerToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[?(@.lectureId=='" + ownerLectureId + "')].title").value(hasItem("Owner Lecture")))
      .andExpect(jsonPath("$[?(@.lectureId=='" + otherLectureId + "')]" ).isEmpty());

    mockMvc
      .perform(get("/api/lectures").header("Authorization", "Bearer " + otherInstructorToken))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[?(@.lectureId=='" + otherLectureId + "')].title").value(hasItem("Other Lecture")))
      .andExpect(jsonPath("$[?(@.lectureId=='" + ownerLectureId + "')]" ).isEmpty());
  }

  private String createLecture(String token, String title) throws Exception {
    String requestBody = """
      {
        "title": "%s"
      }
      """.formatted(title);

    String response = mockMvc
      .perform(
        post("/api/lectures")
          .contentType("application/json")
          .header("Authorization", "Bearer " + token)
          .content(requestBody)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    return extractField(response, "lectureId");
  }

  private void addQuestion(
    String token,
    String lectureId,
    String prompt,
    String modelAnswer,
    int timeLimitSeconds
  ) throws Exception {
    String requestBody = """
      {
        "prompt": "%s",
        "modelAnswer": "%s",
        "timeLimitSeconds": %d
      }
      """.formatted(prompt, modelAnswer, timeLimitSeconds);

    mockMvc
      .perform(
        post("/api/lectures/{lectureId}/questions", lectureId)
          .contentType("application/json")
          .header("Authorization", "Bearer " + token)
          .content(requestBody)
      )
      .andExpect(status().isOk());
  }

  private String createInvite(String token, String lectureId) throws Exception {
    String response = mockMvc
      .perform(
        post("/api/lectures/{lectureId}/invites", lectureId)
          .header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    return extractField(response, "inviteId");
  }

  private String extractField(String response, String fieldName) {
    int keyIndex = response.indexOf("\"" + fieldName + "\"");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
  }
}
