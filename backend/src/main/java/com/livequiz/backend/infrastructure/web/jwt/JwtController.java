package com.livequiz.backend.infrastructure.web.jwt;

import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class JwtController {

  private final JwtService jwtService;
  private final AuthenticationManager authService;

  public JwtController(
    JwtService jwtService,
    AuthenticationManager authService
  ) {
    this.jwtService = jwtService;
    this.authService = authService;
  }

  public record LoginDto(String username, String password) {}

  @PostMapping("/login")
  public Map<String, String> login(@RequestBody LoginDto dto) {
    String username = dto.username;
    String password = dto.password;

    try {
      authService.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)
      );
      String token = jwtService.createToken(username);
      return Map.of("token", token);
    } catch (Exception e) {
      throw new RuntimeException("Invalid credentials");
    }
  }
}
