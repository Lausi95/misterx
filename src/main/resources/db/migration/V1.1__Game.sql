CREATE TABLE game
(
    id         VARCHAR(255) PRIMARY KEY,
    tenant     VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    start_time TIMESTAMP    NOT NULL,
    end_time   TIMESTAMP    NOT NULL
);

CREATE TABLE map
(
    id                 VARCHAR(255) PRIMARY KEY,
    game_id            VARCHAR(255) NOT NULL REFERENCES game (id),
    corner_a_latitude  FLOAT        NOT NULL,
    corner_a_longitude FLOAT        NOT NULL,
    corner_b_latitude  FLOAT        NOT NULL,
    corner_b_longitude FLOAT        NOT NULL,
    rows               INT          NOT NULL,
    columns            INT          NOT NULL
);
