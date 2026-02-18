package com.livequiz.backend.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InviteTokenService {

  private static final String JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int JOIN_CODE_SIZE = 6;

  private final SecureRandom secureRandom = new SecureRandom();

  public String generateOpaqueToken() {
    return UUID.randomUUID() + "-" + UUID.randomUUID();
  }

  public String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }

  public String generateJoinCode() {
    StringBuilder builder = new StringBuilder(JOIN_CODE_SIZE);
    for (int i = 0; i < JOIN_CODE_SIZE; i++) {
      int index = this.secureRandom.nextInt(JOIN_CODE_ALPHABET.length());
      builder.append(JOIN_CODE_ALPHABET.charAt(index));
    }
    return builder.toString();
  }
}
