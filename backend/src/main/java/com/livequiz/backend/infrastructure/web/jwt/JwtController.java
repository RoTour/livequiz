package com.livequiz.backend.infrastructure.web.jwt;

import com.livequiz.backend.application.LiveQuizProperties;
import com.livequiz.backend.application.ResolveUserRoleUseCase;
import com.livequiz.backend.infrastructure.web.ApiException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:4200")
public class JwtController {

  private static final Logger LOG = LoggerFactory.getLogger(JwtController.class);

  private final JwtService jwtService;
  private final AuthenticationManager authService;
  private final ResolveUserRoleUseCase resolveUserRoleUseCase;
  private final LiveQuizProperties liveQuizProperties;

  public JwtController(
    JwtService jwtService,
    AuthenticationManager authService,
    ResolveUserRoleUseCase resolveUserRoleUseCase,
    LiveQuizProperties liveQuizProperties
  ) {
    this.jwtService = jwtService;
    this.authService = authService;
    this.resolveUserRoleUseCase = resolveUserRoleUseCase;
    this.liveQuizProperties = liveQuizProperties;
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
      String role = resolveRole(authentication, username);
      String token = jwtService.createToken(username, role);
      return Map.of("token", token);
    } catch (AuthenticationException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }
  }

  private String resolveRole(Authentication authentication, String username) {
    if (!this.liveQuizProperties.teacherRoleClassificationEnabled()) {
      String legacyRole = authentication
        .getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith("ROLE_"))
        .findFirst()
        .map(authority -> authority.substring("ROLE_".length()))
        .orElse(ResolveUserRoleUseCase.STUDENT_ROLE);
      LOG.info(
        "Auth role classification principal={} role={} source=legacy-authorities",
        username,
        legacyRole
      );
      return legacyRole;
    }

    String resolvedRole = this.resolveUserRoleUseCase.execute(username);
    LOG.info(
      "Auth role classification principal={} role={} source=teacher-registry",
      username,
      resolvedRole
    );
    return resolvedRole;
  }
}
