# 9. Distroless nonroot runtime, layered Spring Boot jar, cached self-contained build

Date: 2026-06-22

## Status

Accepted

## Context

The container image had to satisfy three goals: minimal, fast to build, and as secure as
possible. The original `Dockerfile` worked against all three:

- `FROM openjdk:21-rc-slim` — a **deprecated** image family, on a **release-candidate** JDK,
  carrying a full JDK (compilers, tools) at runtime.
- It ran the whole Gradle build inside the image (`COPY . . && ./gradlew build`) with no
  dependency-layer caching, so every build re-resolved all dependencies.
- It ran as **root**.
- It used `ENTRYPOINT ["sh", "-c"]` + a shell-form command, so the JVM was not PID 1 and
  SIGTERM signal handling (graceful shutdown) was unreliable.
- There was no `.dockerignore`, so `COPY . .` shipped `.git/`, `build/`, `.idea/`, and the
  `ansible/` deploy config into the build context and image layers.

Decisions taken, with alternatives:

- **Build location.** Build *inside* the Dockerfile (multi-stage) with BuildKit cache mounts,
  rather than building the jar on the CI runner and copying it in. Keeps the image
  self-building and reproducible locally; relies on `cache-from`/`cache-to` (GHA cache backend)
  to make ephemeral-runner builds fast.
- **Runtime base image.** `gcr.io/distroless/java21-debian12:nonroot`, rather than
  `eclipse-temurin:21-jre-alpine` (keeps a shell) or a custom `jlink` runtime (smallest, but
  fiddly to get the module list right under Spring reflection).
- **App layout.** Spring Boot **layered jar** extraction (`java -Djarmode=tools … extract
  --layers`), which on Spring Boot 4 produces a thin `application/application.jar` plus a
  `dependencies/lib/` directory it references via its manifest `Class-Path`, rather than a single
  fat jar. Run with `java -jar application.jar` (the layers are flattened into one runtime dir so
  the jar sits next to its `lib/`).

## Decision

Two-stage build. The **builder** uses a full JDK (`eclipse-temurin:21-jdk`), resolves
dependencies in a cache-mounted Gradle home, builds the bootJar, and extracts it into Spring
Boot layers. The **runtime** stage is `gcr.io/distroless/java21-debian12:nonroot` and copies
the extracted layers deepest-changing-last (dependencies first, `application` last) into a
single working dir.

The entrypoint is **exec form** (`java -jar application.jar`), so the JVM is PID 1 and
receives SIGTERM for Spring Boot graceful shutdown. No shell is involved.

An allowlist-style `.dockerignore` restricts the build context to `src/`, `gradle/`, `gradlew`,
`build.gradle.kts`, and `settings.gradle.kts`.

## Consequences

- **No shell, no package manager, nonroot (uid 65532) in the runtime image.** Attack surface is
  minimal and the non-root requirement is satisfied by the base image, not a hand-rolled `USER`.
- **No `docker exec`-able shell for debugging.** Accepted: the app ships Actuator + structured
  logs, so in-container shell access is rarely needed; when it is, run a one-off `temurin`
  container against the same data. (See ADR for the health/observability stance below.)
- **No in-container `HEALTHCHECK`** — distroless has no curl/shell. Health is gated by Traefik's
  service healthcheck against `/actuator/health`, and crash recovery by a Compose
  `restart: unless-stopped` policy. Actuator web exposure is locked to `health` only so
  `env`/`beans`/`mappings` are never reachable.
- **Fast rebuilds**: editing application code only invalidates the small `application` layer; the
  dependencies layer is reused. The first build (or a cache miss on an ephemeral runner) is still
  full cost — hence the GHA build cache.
- The image no longer depends on a release-candidate or deprecated base.
