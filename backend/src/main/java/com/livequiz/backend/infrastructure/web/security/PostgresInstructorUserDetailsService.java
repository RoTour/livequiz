package com.livequiz.backend.infrastructure.web.security;

import com.livequiz.backend.domain.instructor.InstructorAccountRepository;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Profile("postgres")
public class PostgresInstructorUserDetailsService implements UserDetailsService {

  private final InstructorAccountRepository instructorAccountRepository;

  public PostgresInstructorUserDetailsService(
    InstructorAccountRepository instructorAccountRepository
  ) {
    this.instructorAccountRepository = instructorAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    String normalizedEmail = normalize(email);
    return this.instructorAccountRepository
      .findByEmail(normalizedEmail)
      .filter(account -> account.active())
      .map(
        account ->
          User
            .withUsername(account.email())
            .password(account.passwordHash())
            .roles("INSTRUCTOR")
            .build()
      )
      .orElseThrow(() -> new UsernameNotFoundException("Instructor account not found"));
  }

  private String normalize(String email) {
    if (email == null || email.isBlank()) {
      throw new UsernameNotFoundException("Instructor email is required");
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
