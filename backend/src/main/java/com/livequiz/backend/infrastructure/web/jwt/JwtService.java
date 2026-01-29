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
      .signWith(Jwts.SIG.HS256.key().build())
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
