-- Standardize temporal columns on TIMESTAMP WITH TIME ZONE so OffsetDateTime values are
-- persisted as deterministic UTC instants (with hibernate.jdbc.time_zone=UTC), independent
-- of the JVM's default zone. game.start_time/end_time and agent_location.timestamp were
-- TIMESTAMP (without time zone); team_member.registered_at and agent_finding.found_at are
-- already timestamptz. Existing naive values are interpreted as UTC.

ALTER TABLE game
    ALTER COLUMN start_time TYPE TIMESTAMP WITH TIME ZONE USING start_time AT TIME ZONE 'UTC',
    ALTER COLUMN end_time TYPE TIMESTAMP WITH TIME ZONE USING end_time AT TIME ZONE 'UTC';

ALTER TABLE agent_location
    ALTER COLUMN timestamp TYPE TIMESTAMP WITH TIME ZONE USING timestamp AT TIME ZONE 'UTC';
