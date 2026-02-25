# Backend Run Guide

## Choose the runtime profile

The backend defaults to the `postgres` profile (`spring.profiles.active=postgres` in `application.properties`).

You can override the profile in any of these ways:

1. Environment variable (works for Maven and packaged jar):

```bash
SPRING_PROFILES_ACTIVE=in-memory ./mvnw spring-boot:run
```

2. Spring Boot Maven plugin profile flag:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=in-memory
```

3. Generic Spring argument:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=in-memory"
```

If you run the packaged jar:

```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=in-memory
```

## Available profiles

- `postgres` (default): PostgreSQL + Flyway migrations.
- `in-memory`: in-memory repositories, no datasource/Flyway.
- `memory`: alias behavior of `in-memory`.

## Common launch examples

Run with default profile (`postgres`):

```bash
./mvnw spring-boot:run
```

Run with in-memory profile:

```bash
SPRING_PROFILES_ACTIVE=in-memory ./mvnw spring-boot:run
```

Run with postgres profile explicitly:

```bash
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

## Notes

- For non-in-memory profiles, set `LIVEQUIZ_JWT_SECRET` before startup.
- For `postgres`, you can override DB settings with:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

## Student Email Verification via AWS SES (SMTP)

To enable real email delivery for student verification, configure SES SMTP and enable the SMTP sender:

```bash
export LIVEQUIZ_STUDENT_EMAIL_VERIFICATION_SMTP_ENABLED=true
export LIVEQUIZ_STUDENT_EMAIL_VERIFICATION_FROM='no-reply@your-verified-domain.com'
export LIVEQUIZ_STUDENT_EMAIL_VERIFICATION_URL_BASE='https://your-frontend-domain/student/verify-email'

export SPRING_MAIL_HOST='email-smtp.eu-west-1.amazonaws.com'
export SPRING_MAIL_PORT='587'
export SPRING_MAIL_USERNAME='<ses-smtp-username>'
export SPRING_MAIL_PASSWORD='<ses-smtp-password>'
export SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH='true'
export SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE='true'
```

Fail-fast validation is enabled at startup when `LIVEQUIZ_STUDENT_EMAIL_VERIFICATION_SMTP_ENABLED=true`.
The backend will refuse to start if any required SMTP property is missing/invalid.
