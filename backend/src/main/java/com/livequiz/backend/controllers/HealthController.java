package com.livequiz.backend.controllers;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping({ "/health", "/api/health" })
  public Map<String, String> health() {
    return Map.of(
      "status",
      "OK",
      "timestamp",
      LocalDateTime.now().toString()
    );
  }
}
