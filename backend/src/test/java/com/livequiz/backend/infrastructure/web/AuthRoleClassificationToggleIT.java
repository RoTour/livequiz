package com.livequiz.backend.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.livequiz.backend.infrastructure.web.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("in-memory")
@TestPropertySource(properties = "livequiz.teacher-role-classification-enabled=false")
class AuthRoleClassificationToggleIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @Test
  void should_fallback_to_authority_role_when_teacher_classification_toggle_is_disabled()
    throws Exception {
    String token = login("instructor-candidate", "password");

    JwtService.TokenClaims claims = this.jwtService.validateToken(token);
    assertNotNull(claims);
    assertEquals("INSTRUCTOR", claims.role());
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
    return extractField(response, "token");
  }

  private String extractField(String response, String fieldName) {
    int keyIndex = response.indexOf("\"" + fieldName + "\"");
    int start = response.indexOf(":\"", keyIndex) + 2;
    int end = response.indexOf('"', start);
    return response.substring(start, end);
  }
}
