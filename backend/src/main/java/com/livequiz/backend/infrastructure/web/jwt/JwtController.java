package com.livequiz.backend.infrastructure.web.jwt;

import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.livequiz.backend.infrastructure.web.ApiException;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:4200")
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
      Authentication authentication = authService.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)
      );
      String role = authentication
        .getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith("ROLE_"))
        .findFirst()
        .map(authority -> authority.substring("ROLE_".length()))
        .orElse("STUDENT");
      String token = jwtService.createToken(username, role);
      return Map.of("token", token);
    } catch (Exception e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }
  }
}
