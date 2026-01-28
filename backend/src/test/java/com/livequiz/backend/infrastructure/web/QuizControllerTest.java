package com.livequiz.backend.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void should_return_bad_request_when_title_is_empty() throws Exception {
    String requestBody = """
      {
        "title": ""
      }
      """;

    mockMvc
      .perform(
        post("/api/quizzes")
          .contentType("application/json")
          .content(requestBody)
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("DOMAIN_ERROR"))
      .andExpect(jsonPath("$.message").value("Title cannot be null or blank"));
  }

  @Test
  void should_return_not_found_for_invalid_endpoint() throws Exception {
    mockMvc
      .perform(post("/api/invalid-endpoint"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("NOT_FOUND"))
      .andExpect(
        jsonPath("$.message").value("The requested resource was not found")
      );
  }
}
