#!/usr/bin/env bash
set -euo pipefail

if [[ $# -gt 0 ]]; then
  >&2 printf 'Do not pass password as CLI argument. Use interactive mode or stdin.\n'
  exit 1
fi

if [[ -t 0 ]]; then
  read -r -s -p "Password to hash: " PASSWORD
  printf '\n'
else
  read -r PASSWORD
fi

if [[ -z "${PASSWORD}" ]]; then
  >&2 printf 'Password cannot be empty\n'
  exit 1
fi

./mvnw -q -DskipTests compile && printf '%s\n' "${PASSWORD}" | ./mvnw -q -DskipTests -Dexec.mainClass=com.livequiz.backend.infrastructure.cli.PasswordHashCli org.codehaus.mojo:exec-maven-plugin:3.5.0:java
