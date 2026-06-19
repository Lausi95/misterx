# 3. Standardize temporal types on OffsetDateTime with UTC persistence

Date: 2026-06-19

## Status

Accepted

## Context

Temporal fields were modelled with three different `java.time` types: `Game` used
`OffsetDateTime` (`startTime`/`endTime`), agent locations and findings used
`ZonedDateTime` (`timestamp`/`foundAt`), and `TeamMember.registeredAt` used `Instant`.
The persistence layer was equally inconsistent and quietly lossy:

- `game.start_time/end_time` and `agent_location.timestamp` were `TIMESTAMP` **without**
  time zone, while `team_member.registered_at` and `agent_finding.found_at` were already
  `TIMESTAMP WITH TIME ZONE`.
- No `hibernate.jdbc.time_zone` was configured, so Hibernate used the JVM default zone
  when writing to the zone-less columns. The stored value therefore depended on whatever
  zone the JVM happened to run in — the same `OffsetDateTime` would persist as a different
  wall-clock value on a machine in `Europe/Berlin` than on one in UTC, and read back
  ambiguously. This affected `Game` too, the type the rest of the code was meant to
  standardize on.

So even the "correct" type was not being persisted **deterministically**. (Note: Postgres
`timestamptz` does not store an offset either — it stores a UTC instant and normalizes on
read. The problem with the zone-less columns was not a lost offset but a JVM-zone-dependent,
ambiguous instant.)

Options considered:

- **Type rename only** — swap the Kotlin types to `OffsetDateTime`, leave columns and
  config untouched. Rejected: smallest diff but preserves the offset-loss bug.
- **`Instant` everywhere** — `Instant` is arguably the more semantically pure type for an
  event instant (`registeredAt`, `foundAt`) since it carries no offset to begin with.
  Rejected: the goal is a single uniform type across the domain, and `Game` start/end
  times benefit from an explicit offset.
- **Type rename + offset-correct persistence** — chosen.

## Decision

Use `OffsetDateTime` for every temporal field across domain models, ports, JPA entities,
web resources/requests, and tests. Make storage offset-correct to match:

- Flyway `V1.5` migrates `game.start_time/end_time` and `agent_location.timestamp` to
  `TIMESTAMP WITH TIME ZONE` (existing naive values interpreted as UTC; tables are
  effectively empty so the interpretation is moot in practice).
- `spring.jpa.properties.hibernate.jdbc.time_zone: UTC` so Hibernate reads and writes
  through the JDBC driver in UTC, making the stored instant deterministic and independent
  of the JVM's default zone.
- "Now" is captured with `OffsetDateTime.now(ZoneOffset.UTC)` in the three services that
  stamp timestamps (`FindAgentService`, `UpdateAgentLocationService`,
  `RegisterTeamMemberService`). `OffsetDateTime.now()` with no argument would capture the
  JVM's offset, not UTC.

No `Clock` abstraction was introduced — the services still call `now()` directly, matching
the existing codebase style. Making "now" injectable for deterministic tests is left as a
separate, optional change.

## Consequences

- **Instants are now persisted deterministically as UTC** for all temporal fields, not just
  the ones that were already `timestamptz`. Note that an `OffsetDateTime` written with a
  non-UTC offset reads back normalized to UTC (`Z`) — same instant, different offset — so
  compare reloaded values with `isEqual()`, not `equals()`. (The repository round-trip test
  compares via `.toInstant()`, which is offset-agnostic.)
- **API wire format is unchanged.** With Spring Boot's defaults (`write-dates-as-timestamps`
  off, zone-id off) both `ZonedDateTime` and `OffsetDateTime` serialize as
  `2026-06-19T10:15:30+02:00`, so existing clients see no difference.
- **`Instant` semantics traded for uniformity.** `registeredAt`/`foundAt` now carry an
  offset (always UTC) where before they were zoneless instants. Functionally equivalent for
  these fields, but a deliberate move away from the more minimal type.
- **"Now" remains non-deterministic in tests.** Without a `Clock`, tests still exercise the
  real wall clock; a future change can inject `Clock` if controllable time is needed.
