package com.livequiz.backend.infrastructure.web.jwt;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final SecretKey secret = Jwts.SIG.HS256.key().build();

  public String createToken(String subject) {
    return Jwts.builder()
      .subject(subject)
      .issuedAt(new java.util.Date())
      .expiration(new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
      .signWith(secret)
      .compact();
  }

  public String validateToken(String token) {
    try {
      return Jwts.parser()
        .verifyWith(secret)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
    } catch (Exception e) {
      return null;
    }
  }
}
