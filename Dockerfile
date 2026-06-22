# syntax=docker/dockerfile:1

# --- Build stage: full JDK + Gradle, with a cache-mounted Gradle home ---
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /src

# Copy build descriptors first so dependency resolution caches independently of source.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY src src

# BuildKit cache mount keeps the Gradle home (downloaded deps) warm across local rebuilds.
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar -x test --no-daemon

# Extract the bootJar into Spring Boot layers so dependencies and application
# code land in separate Docker layers (deps cached, only app layer churns).
RUN java -Djarmode=tools -jar build/libs/application.jar extract --layers --destination extracted

# --- Runtime stage: distroless, nonroot (uid 65532), no shell/package manager ---
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /application
EXPOSE 8080

# Copy extracted layers least-to-most volatile so code changes only bust the last layer.
# The dependencies layer brings `lib/`; the application layer brings the thin
# `application.jar`, whose manifest Class-Path points at that sibling `lib/`.
COPY --from=builder /src/extracted/dependencies/ ./
COPY --from=builder /src/extracted/spring-boot-loader/ ./
COPY --from=builder /src/extracted/snapshot-dependencies/ ./
COPY --from=builder /src/extracted/application/ ./

# Exec form: the JVM is PID 1 and receives SIGTERM for graceful shutdown.
# JVM heap sizing (MaxRAMPercentage) is supplied via JAVA_TOOL_OPTIONS in compose.
ENTRYPOINT ["java", "-jar", "application.jar"]

LABEL org.opencontainers.image.source=https://github.com/Lausi95/city-game-api
LABEL org.opencontainers.image.description="Backend of the city game"
LABEL org.opencontainers.image.licenses=MIT
