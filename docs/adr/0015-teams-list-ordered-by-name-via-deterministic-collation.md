# 15. The teams list is ordered by name, sorted in the database via a deterministic German collation

Date: 2026-06-22

## Status

Accepted

## Context

An Organizer viewing a Game wants the teams list (`GET /games/{gameId}/teams`) to read as a
plain alphabetical roster, ordered by team **name** ascending. `name` is the only meaningful
ordering key — there is no score, creation time, or other field a client would sensibly sort
this list by.

"Alphabetical" hides two requirements that the database default does not satisfy:

1. **Case-insensitive.** `team.name` is a `VARCHAR(255)` under Postgres' default collation,
   which orders by raw byte value. Under that collation every uppercase letter sorts before
   every lowercase one, so `"team apple"` would land *after* `"Team Zebra"` — a human reads
   that as a bug in an A–Z roster.
2. **German-correct.** This is a German-context game; team names plausibly carry umlauts
   (`Müller`, `Straße`, `Über-Team`). ASCII case-folding alone (`LOWER(name)`) still orders
   `ä` *after* `z` by byte, whereas a German reader expects `Ärger` to sit next to `Apfel`.

The list paginates at the database level — `findByGameIdAndTenant(gameId, tenant, pageable)`
slices the page in SQL — so the ordering has to be expressible *in the query the database
runs*, not applied in memory after the fact. This is the opposite situation to the agents
list (ADR 0014), whose ordering key is a derived, off-row value that forced in-memory sorting;
here the key is a real column, so database-level sort-and-paginate is kept.

Options considered for delivering case-insensitive + German ordering:

- **A — JPQL `@Query` with `ORDER BY LOWER(t.name)`.** Smallest blast radius (one query, no
  schema change, `name` equality untouched). Rejected: JPQL cannot express `COLLATE`, so it
  delivers *case-insensitivity only* — umlauts still sort by byte. It cannot meet the
  German-correct requirement at all.
- **B — Native `@Query` with `ORDER BY name COLLATE "de-DE-x-icu"`.** Keeps the column
  collation untouched and applies the locale per query. Rejected: forces raw SQL with a
  hand-written tenant + `game_id` filter *and* a separate `countQuery` for pagination —
  more surface to get wrong than the alternative, and the first native query in a codebase
  that otherwise uses only Spring derived queries.
- **C — Deterministic `de-DE-x-icu` collation on the `team.name` column (chosen).** A Flyway
  `ALTER ... COLLATE` migration makes German + case-folding ordering a property of the column.
  A plain `@PageableDefault(sort = ["name"])` then emits `ORDER BY name`, which the database
  resolves correctly. No `@Query`, derived-query idiom preserved, both requirements satisfied
  in one stroke.

The decisive point against the obvious fear of a collation change — that it would alter `name`
equality semantics — is that it only applies to a **non-deterministic** collation. A
**deterministic** ICU collation breaks comparison ties by raw byte, so `name = 'Foo'` stays
byte-exact (it will *not* match `'foo'`); only `ORDER BY` weights change. Under that collation
case becomes a tertiary tiebreak, so `Apple` and `apple` sort adjacent — the human-friendly
order — while equality is unaffected.

## Decision

`GET /games/{gameId}/teams` orders teams by **name ascending**, resolved in the database:

1. A Flyway migration sets a deterministic German ICU collation on the column:
   `ALTER TABLE team ALTER COLUMN name TYPE VARCHAR(255) COLLATE "de-DE-x-icu";`
   (`de-DE-x-icu` is a predefined, deterministic ICU collation; Postgres is built with ICU.)
2. The controller declares `@PageableDefault(sort = ["name"]) pageable: Pageable`. The
   `Sort` flows unchanged through `GetTeamsUseCase` → `GetTeamsPort` →
   `findByGameIdAndTenant(gameId, tenant, pageable)` as today; no port, query, or adapter
   signature changes.
3. The sort is a **default, not forced** — unlike the agents list (ADR 0014), the incoming
   `Pageable`'s sort is honoured, not ignored. In practice `name` is the only sortable field,
   so this is theoretical headroom rather than a feature.

Ordering is **case-insensitive and German-correct** purely as a consequence of the column
collation; the application code contains no `LOWER()`, no `COLLATE`, and no custom query.

## Consequences

- **The ordering contract lives in the database schema, not the code.** A reader of the
  controller sees only `sort = ["name"]`; the case-folding and umlaut behaviour is invisible
  there and explained only by the column collation and this ADR. The load-bearing test is
  therefore a **repository integration test against real Postgres** (Testcontainers) that
  inserts mixed-case and umlaut names and asserts the German order — an H2 or mocked test
  would prove nothing about `de-DE-x-icu`. A controller test additionally asserts the default
  sort is applied end-to-end.
- **`team.name` equality and uniqueness comparisons remain byte-exact**, because the collation
  is deterministic. A future case-insensitive uniqueness rule on names would *not* come for
  free from this collation and must be designed explicitly.
- **Pagination is not yet provably stable for duplicate names.** `team.name` has no uniqueness
  constraint today, so two teams sharing a name have an undefined relative order across page
  requests. This is accepted for now on the basis that **name uniqueness will be enforced in a
  later change**; once it is, `name` becomes a total order and the point is moot. Until then
  the rare duplicate-name case can page unstably. An `ORDER BY name, id` tiebreak was the
  cheap insurance considered and deliberately deferred in favour of the uniqueness constraint.
- **Portability cost.** The ordering now depends on Postgres being built with ICU and on the
  `de-DE-x-icu` collation existing. Both hold for the deployed `postgres:latest` and the
  Testcontainers image; a different database engine would not reproduce this ordering.
- Contrast with ADR 0014: the agents list sorts a *derived* key in memory and ignores client
  sort; the teams list sorts a *real column* in the database and keeps client sort. The two
  list endpoints deliberately differ because their ordering keys differ in nature.
