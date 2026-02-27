FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -q -DskipTests package \
  && cp "$(ls target/*.jar | grep -v '\\.original$' | head -n 1)" /workspace/backend.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends wget \
  && rm -rf /var/lib/apt/lists/* \
  && groupadd --system livequiz \
  && useradd --system --gid livequiz --create-home livequiz

COPY --from=build /workspace/backend.jar /app/backend.jar

RUN chown livequiz:livequiz /app/backend.jar

USER livequiz

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+UseG1GC", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/backend.jar"]
