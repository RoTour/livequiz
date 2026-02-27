package com.livequiz.backend.infrastructure.web.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.livequiz.backend.domain.instructor.InstructorAccount;
import com.livequiz.backend.domain.instructor.InstructorAccountRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class PostgresInstructorUserDetailsServiceTest {

  @Test
  void should_load_active_instructor_account_by_email() {
    String hash = "$2a$10$rAPUKlMRTFnkaUacaTr5hu1Q2Z7xZbYB1nY98l.c45FAnu.Isc0SC";
    InMemoryInstructorAccountRepository repository = new InMemoryInstructorAccountRepository();
    repository.save(
      new InstructorAccount(
        "instructor@ynov.com",
        hash,
        true,
        Instant.now(),
        Instant.now()
      )
    );
    PostgresInstructorUserDetailsService service = new PostgresInstructorUserDetailsService(
      repository
    );

    var userDetails = service.loadUserByUsername(" Instructor@Ynov.com ");

    assertEquals("instructor@ynov.com", userDetails.getUsername());
    assertEquals(hash, userDetails.getPassword());
    assertEquals("ROLE_INSTRUCTOR", userDetails.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  void should_reject_inactive_or_missing_instructor_account() {
    InMemoryInstructorAccountRepository repository = new InMemoryInstructorAccountRepository();
    repository.save(
      new InstructorAccount(
        "inactive@ynov.com",
        "$2a$10$rAPUKlMRTFnkaUacaTr5hu1Q2Z7xZbYB1nY98l.c45FAnu.Isc0SC",
        false,
        Instant.now(),
        Instant.now()
      )
    );

    PostgresInstructorUserDetailsService service = new PostgresInstructorUserDetailsService(
      repository
    );

    assertThrows(
      UsernameNotFoundException.class,
      () -> service.loadUserByUsername("missing@ynov.com")
    );
    assertThrows(
      UsernameNotFoundException.class,
      () -> service.loadUserByUsername("inactive@ynov.com")
    );
  }

  private static class InMemoryInstructorAccountRepository
    implements InstructorAccountRepository {

    private final Map<String, InstructorAccount> accountsByEmail = new HashMap<>();

    @Override
    public void save(InstructorAccount account) {
      this.accountsByEmail.put(normalize(account.email()), account);
    }

    @Override
    public Optional<InstructorAccount> findByEmail(String email) {
      if (email == null || email.isBlank()) {
        return Optional.empty();
      }
      return Optional.ofNullable(this.accountsByEmail.get(normalize(email)));
    }

    private String normalize(String email) {
      return email.trim().toLowerCase(Locale.ROOT);
    }
  }
}
