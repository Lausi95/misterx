CREATE TABLE agent_finding (
    id                 VARCHAR(255) PRIMARY KEY,
    tenant             VARCHAR(255)             NOT NULL,
    game_id            VARCHAR(255)             NOT NULL REFERENCES game (id),
    team_id            VARCHAR(255)             NOT NULL REFERENCES team (id),
    agent_id           VARCHAR(255)             NOT NULL REFERENCES agent (id),
    found_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    reported_latitude  FLOAT,
    reported_longitude FLOAT,
    agent_latitude     FLOAT,
    agent_longitude    FLOAT,
    CONSTRAINT uq_agent_finding_team_agent UNIQUE (tenant, team_id, agent_id)
);
