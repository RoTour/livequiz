package com.livequiz.backend.infrastructure.web.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
    return Jwts.builder()
      .subject(subject)
      .claims(Map.of("role", role))
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
      return new TokenClaims(claims.getSubject(), claims.get("role", String.class));
    } catch (Exception e) {
      return null;
    }
  }

  public record TokenClaims(String subject, String role) {}
}
