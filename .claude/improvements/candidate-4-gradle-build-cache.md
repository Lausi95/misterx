# Candidate 4 — Enable Gradle build cache in CI

**Strength:** Worth exploring  
**Estimated saving:** ~75 s on runs where only test code changed (main compilation cache hit)

## Problem

There is no `gradle.properties` file in the project root. Without
`org.gradle.caching=true`, Gradle's build cache is disabled even though
`gradle/actions/setup-gradle@v5` already persists and restores the local build
cache directory between CI runs. Every run recompiles all Kotlin sources from
scratch regardless of what changed.

Files involved:
- `gradle.properties` — **does not exist**, needs to be created
- `build.gradle.kts` — reference for tasks that benefit from caching
- `.github/workflows/test.yml` — the workflow that runs `./gradlew test`

## What the build cache actually caches

Gradle's build cache stores **task outputs** keyed by task inputs. For this project:

| Task | Cached when inputs unchanged |
|------|------------------------------|
| `compileKotlin` | No source changes in `src/main/` |
| `compileTestKotlin` | No source changes in `src/test/` |
| `jacocoTestReport` | Same `.exec` file as last run |
| `test` | **Not cached by default** — tests are not cacheable without explicit opt-in |

The biggest win: if a PR only changes test files, `compileKotlin` (the main
sources) gets a cache hit and is skipped — saving ~45 s.

## Proposed solution

Create `gradle.properties` at the project root:

```properties
org.gradle.caching=true
```

`gradle/actions/setup-gradle@v5` automatically:
1. Restores `~/.gradle/caches/build-cache-*` at job start
2. Saves it back at job end

No workflow changes needed.

### Optional additions to `gradle.properties`

These are not required but complement the build cache:

```properties
# Increase Gradle daemon heap for Kotlin compilation on CI
org.gradle.jvmargs=-Xmx2g -XX:+HeapDumpOnOutOfMemoryError
```

## Caveats

- Cache hits require **identical task inputs**. A PR that changes `src/main/`
  will miss the `compileKotlin` cache — no saving on those runs.
- `test` tasks are not cacheable by default (side effects, non-deterministic).
  The saving comes from compilation tasks only.
- Do not add `actions/cache` manually for `~/.gradle` — `setup-gradle@v5`
  already handles this and layering a second cache action causes thrashing.

## How to explore in a future session

This is a simple implementation — no grilling needed. Just:

1. Create `gradle.properties` with `org.gradle.caching=true`
2. Push a PR, watch CI, verify the second run shows `FROM CACHE` in Gradle output
   for `compileKotlin` if main sources were unchanged

To verify locally:
```bash
./gradlew test --build-cache
./gradlew test --build-cache  # second run: compileKotlin should say FROM CACHE
```
