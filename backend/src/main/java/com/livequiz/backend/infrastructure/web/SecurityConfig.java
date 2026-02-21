package com.livequiz.backend.infrastructure.web;

import com.livequiz.backend.infrastructure.web.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
            .requestMatchers("/health", "/api/health", "/api/auth/**")
            .permitAll()
            .requestMatchers(
              "/api/lectures",
              "/api/lectures/*/questions/**",
              "/api/lectures/*/state",
              "/api/lectures/*/invites/**"
            )
            .hasRole("INSTRUCTOR")
            .requestMatchers(
              "/api/lectures/join",
              "/api/lectures/*/students/me/next-question",
              "/api/lectures/*/submissions"
            )
            .hasAnyRole("STUDENT", "INSTRUCTOR")
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
  public InMemoryUserDetailsManager userDetailsService() {
    UserDetails instructor = User.withDefaultPasswordEncoder()
      .username("instructor")
      .password("password")
      .roles("INSTRUCTOR")
      .build();

    UserDetails student = User.withDefaultPasswordEncoder()
      .username("student")
      .password("password")
      .roles("STUDENT")
      .build();

    UserDetails studentTwo = User.withDefaultPasswordEncoder()
      .username("student2")
      .password("password")
      .roles("STUDENT")
      .build();

    UserDetails instructorTwo = User.withDefaultPasswordEncoder()
      .username("instructor2")
      .password("password")
      .roles("INSTRUCTOR")
      .build();
    return new InMemoryUserDetailsManager(
      instructor,
      instructorTwo,
      student,
      studentTwo
    );
  }

  @Bean
  public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }
}
