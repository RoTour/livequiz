package com.livequiz.backend.infrastructure.cli;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class PasswordHashCli {

  private PasswordHashCli() {}

  public static void main(String[] args) {
    if (args.length > 0) {
      throw new IllegalArgumentException("Pass password via stdin or interactive prompt only");
    }
    String rawPassword = readPassword();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String hash = passwordEncoder.encode(rawPassword);
    System.out.println(hash);
  }

  private static String readPassword() {
    Console console = System.console();
    if (console != null) {
      char[] password = console.readPassword("Enter password to hash: ");
      if (password == null || password.length == 0) {
        throw new IllegalArgumentException("Password cannot be empty");
      }
      return new String(password);
    }

    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    if (!scanner.hasNextLine()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }

    String line = scanner.nextLine();
    if (line.isBlank()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    return line;
  }
}
