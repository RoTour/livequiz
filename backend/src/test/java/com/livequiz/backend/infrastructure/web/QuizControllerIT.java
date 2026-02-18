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
public class QuizControllerIT {

  @Autowired
  private MockMvc mockMvc;

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
}
