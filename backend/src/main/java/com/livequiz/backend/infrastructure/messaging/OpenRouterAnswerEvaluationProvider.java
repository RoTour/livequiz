package com.livequiz.backend.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livequiz.backend.application.AnswerEvaluationProvider;
import com.livequiz.backend.application.AnswerEvaluationStatus;
import com.livequiz.backend.domain.submission.Feedback;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "livequiz.answer-evaluation.provider", havingValue = "openrouter")
public class OpenRouterAnswerEvaluationProvider implements AnswerEvaluationProvider {

  private static final String STATUS_CORRECT = "CORRECT";
  private static final String STATUS_INCOMPLETE = "INCOMPLETE";
  private static final String STATUS_NEEDS_IMPROVEMENT = "NEEDS_IMPROVEMENT";

  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  private final String apiUrl;
  private final String apiKey;
  private final String model;
  private final String appName;
  private final String siteUrl;

  public OpenRouterAnswerEvaluationProvider(
    RestClient.Builder restClientBuilder,
    ObjectMapper objectMapper,
    @Value("${livequiz.answer-evaluation.openrouter.api-url:https://openrouter.ai/api/v1/chat/completions}") String apiUrl,
    @Value("${livequiz.answer-evaluation.openrouter.api-key:}") String apiKey,
    @Value("${livequiz.answer-evaluation.openrouter.model:openai/gpt-4o-mini}") String model,
    @Value("${livequiz.answer-evaluation.openrouter.app-name:LiveQuiz}") String appName,
    @Value("${livequiz.answer-evaluation.openrouter.site-url:}") String siteUrl
  ) {
    this.restClient = restClientBuilder.build();
    this.objectMapper = objectMapper;
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
    this.model = model;
    this.appName = appName;
    this.siteUrl = siteUrl;
  }

  @Override
  public EvaluationResult evaluate(String prompt, String modelAnswer, String answerText) {
    if (this.apiKey == null || this.apiKey.isBlank()) {
      throw new IllegalStateException("OpenRouter API key is missing");
    }

    Map<String, Object> payload = buildPayload(prompt, modelAnswer, answerText);
    JsonNode response = this.restClient
      .post()
      .uri(this.apiUrl)
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", "Bearer " + this.apiKey)
      .header("X-Title", this.appName)
      .headers(headers -> {
        if (this.siteUrl != null && !this.siteUrl.isBlank()) {
          headers.add("HTTP-Referer", this.siteUrl);
        }
      })
      .body(payload)
      .retrieve()
      .body(JsonNode.class);

    JsonNode contentNode = response
      .path("choices")
      .path(0)
      .path("message")
      .path("content");
    if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
      throw new IllegalStateException("OpenRouter returned an empty evaluation payload");
    }

    JsonNode resultNode;
    try {
      resultNode = this.objectMapper.readTree(contentNode.asText());
    } catch (Exception exception) {
      throw new IllegalStateException("OpenRouter response is not valid JSON", exception);
    }

    String normalizedStatus = normalizeStatus(resultNode.path("status").asText(""));
    String comment = resultNode.path("comment").asText("");
    List<String> missingKeyPoints = new ArrayList<>();
    JsonNode missingKeyPointsNode = resultNode.path("missingKeyPoints");
    if (missingKeyPointsNode.isArray()) {
      missingKeyPointsNode.forEach(node -> {
        String value = node.asText("").trim();
        if (!value.isBlank()) {
          missingKeyPoints.add(value);
        }
      });
    }

    return new EvaluationResult(
      AnswerEvaluationStatus.valueOf(normalizedStatus),
      new Feedback(STATUS_CORRECT.equals(normalizedStatus), missingKeyPoints, comment),
      this.model
    );
  }

  private Map<String, Object> buildPayload(String prompt, String modelAnswer, String answerText) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("model", this.model);
    payload.put(
      "messages",
      List.of(
        Map.of(
          "role",
          "system",
          "content",
          "You are evaluating a student answer for an instructor. Return only JSON with fields: status, comment, missingKeyPoints. " +
          "status must be one of CORRECT, INCOMPLETE, NEEDS_IMPROVEMENT. missingKeyPoints must be an array of strings."
        ),
        Map.of(
          "role",
          "user",
          "content",
          "Question: " +
          prompt +
          "\nExpected answer: " +
          modelAnswer +
          "\nStudent answer: " +
          answerText
        )
      )
    );
    payload.put("temperature", 0.1);
    payload.put("response_format", Map.of("type", "json_object"));
    return payload;
  }

  private String normalizeStatus(String rawStatus) {
    if (rawStatus == null) {
      return STATUS_NEEDS_IMPROVEMENT;
    }
    String normalized = rawStatus.trim().toUpperCase();
    if (STATUS_CORRECT.equals(normalized)) {
      return STATUS_CORRECT;
    }
    if (STATUS_INCOMPLETE.equals(normalized)) {
      return STATUS_INCOMPLETE;
    }
    if (STATUS_NEEDS_IMPROVEMENT.equals(normalized)) {
      return STATUS_NEEDS_IMPROVEMENT;
    }
    return STATUS_NEEDS_IMPROVEMENT;
  }
}
