package com.livequiz.backend.infrastructure.web.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws java.io.IOException, ServletException {
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response); // next()
      return;
    }

    String token = authHeader.substring(7);
    JwtService.TokenClaims claims = jwtService.validateToken(token);
    if (claims == null || claims.subject() == null || claims.subject().isBlank()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String role = claims.role() == null || claims.role().isBlank() ? "STUDENT" : claims.role();

    UsernamePasswordAuthenticationToken authentication =
      new UsernamePasswordAuthenticationToken(
        claims.subject(),
        null,
        List.of(new SimpleGrantedAuthority("ROLE_" + role))
      );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response); // next()
  }
}
