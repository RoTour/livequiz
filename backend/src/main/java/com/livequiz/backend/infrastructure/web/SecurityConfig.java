package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.infrastructure.web.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    JwtAuthenticationFilter jwtAuthenticationFilter
  ) throws Exception {
    http
      .csrf(csrf -> csrf.disable()) // Disable CSRF for non-browser APIs
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .httpBasic(httpBasic -> httpBasic.disable())
      .formLogin(formLogin -> formLogin.disable())
      .authorizeHttpRequests(
        auth ->
          auth
            .requestMatchers("/health", "/api/health")
            .permitAll()
            .requestMatchers(
              "/api/auth/login",
              "/api/auth/anonymous",
              "/api/auth/students/verify-email"
            )
            .permitAll()
            .requestMatchers(
              "/api/auth/students/register-email",
              "/api/auth/students/resend-verification"
            )
            .hasRole("STUDENT")
            .requestMatchers(
              "/api/lectures/join",
              "/api/lectures/students/me",
              "/api/lectures/*/students/me/next-question",
              "/api/lectures/*/students/me/answer-statuses",
              "/api/lectures/*/submissions"
            )
            .hasAnyRole("STUDENT", "INSTRUCTOR")
            .requestMatchers(
              "/api/lectures",
              "/api/lectures/*/questions/**",
              "/api/lectures/*/state",
              "/api/lectures/*/invites/**"
            )
            .hasRole("INSTRUCTOR")
            .anyRequest()
            .authenticated()
      )
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      );
    return http.build();
  }

  @Bean
  @Profile({ "in-memory", "memory" })
  public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails instructor = User.builder()
      .username("instructor@ynov.com")
      .password(passwordEncoder.encode("password"))
      .roles("INSTRUCTOR")
      .build();

    UserDetails student = User.builder()
      .username("student")
      .password(passwordEncoder.encode("password"))
      .roles("STUDENT")
      .build();

    UserDetails studentTwo = User.builder()
      .username("student2")
      .password(passwordEncoder.encode("password"))
      .roles("STUDENT")
      .build();

    UserDetails instructorTwo = User.builder()
      .username("instructor2@ynov.com")
      .password(passwordEncoder.encode("password"))
      .roles("INSTRUCTOR")
      .build();

    UserDetails instructorCandidate = User.builder()
      .username("instructor-candidate@ynov.com")
      .password(passwordEncoder.encode("password"))
      .roles("INSTRUCTOR")
      .build();
    return new InMemoryUserDetailsManager(
      instructor,
      instructorTwo,
      instructorCandidate,
      student,
      studentTwo
    );
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }
}
