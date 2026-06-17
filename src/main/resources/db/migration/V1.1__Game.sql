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

CREATE TABLE agent
(
    id           VARCHAR(255) PRIMARY KEY,
    tenant       VARCHAR(255) NOT NULL,
    game_id      VARCHAR(255) NOT NULL REFERENCES game (id),
    type         VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    alias        VARCHAR(255) NOT NULL,
    active       BOOLEAN      NOT NULL
);

CREATE TABLE agent_location
(
    id        VARCHAR(255) PRIMARY KEY,
    tenant    VARCHAR(255),
    agent_id  VARCHAR(255) REFERENCES agent (id),
    timestamp TIMESTAMP NOT NULL,
    latitude  FLOAT     NOT NULL,
    longitude FLOAT     NOT NULL
);
