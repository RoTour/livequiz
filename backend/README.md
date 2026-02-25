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
