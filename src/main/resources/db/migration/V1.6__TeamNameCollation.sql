-- Order the teams list (GET /games/{gameId}/teams) by name in a German-correct,
-- case-insensitive way. A deterministic ICU collation makes ORDER BY name fold case
-- (case becomes a tertiary tiebreak) and sort umlauts next to their base letter,
-- while keeping name equality byte-exact. See ADR 0015.
ALTER TABLE team
    ALTER COLUMN name TYPE VARCHAR(255) COLLATE "de-DE-x-icu";
