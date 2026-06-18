CREATE TABLE team_member
(
    id            VARCHAR(255) PRIMARY KEY,
    tenant        VARCHAR(255)             NOT NULL,
    game_id       VARCHAR(255)             NOT NULL REFERENCES game (id),
    team_id       VARCHAR(255)             NOT NULL REFERENCES team (id),
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL
);
