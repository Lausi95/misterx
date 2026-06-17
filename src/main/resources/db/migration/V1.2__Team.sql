CREATE TABLE team
(
    id      VARCHAR(255) PRIMARY KEY,
    tenant  VARCHAR(255) NOT NULL,
    game_id VARCHAR(255) NOT NULL REFERENCES game (id),
    name    VARCHAR(255) NOT NULL
);
