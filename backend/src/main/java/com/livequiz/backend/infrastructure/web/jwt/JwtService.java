package com.livequiz.backend.infrastructure.web.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secret;

  public JwtService(
    @Value("${livequiz.jwt.secret:}") String secret,
    Environment environment
  ) {
    boolean inMemoryProfile =
      java.util.Arrays.stream(environment.getActiveProfiles())
        .anyMatch(profile -> profile.equals("in-memory") || profile.equals("memory"));
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("LIVEQUIZ_JWT_SECRET must be configured");
    }
    if (!inMemoryProfile && secret.equals("change-me-in-production-change-me-in-production")) {
      throw new IllegalStateException("LIVEQUIZ_JWT_SECRET must not use insecure default value");
    }
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.secret = Keys.hmacShaKeyFor(keyBytes);
  }

  public String createToken(String subject, String role) {
    return createToken(subject, role, Map.of());
  }

  public String createStudentToken(
    String studentId,
    boolean anonymous,
    boolean emailVerified
  ) {
    return createToken(
      studentId,
      "STUDENT",
      Map.of("anonymous", anonymous, "emailVerified", emailVerified)
    );
  }

  private String createToken(
    String subject,
    String role,
    Map<String, Object> additionalClaims
  ) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    claims.putAll(additionalClaims);
    return Jwts.builder()
      .subject(subject)
      .claims(claims)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 10))
      .signWith(this.secret)
      .compact();
  }

  public TokenClaims validateToken(String token) {
    try {
      var claims = Jwts.parser()
        .verifyWith(this.secret)
        .build()
        .parseSignedClaims(token)
        .getPayload();
      return new TokenClaims(
        claims.getSubject(),
        claims.get("role", String.class),
        Boolean.TRUE.equals(claims.get("anonymous", Boolean.class)),
        Boolean.TRUE.equals(claims.get("emailVerified", Boolean.class))
      );
    } catch (Exception e) {
      return null;
    }
  }

  public record TokenClaims(
    String subject,
    String role,
    boolean anonymous,
    boolean emailVerified
  ) {}
}
