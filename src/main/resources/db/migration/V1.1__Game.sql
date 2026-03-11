CREATE TABLE game
(
    id         VARCHAR(255) PRIMARY KEY,
    tenant     VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    start_time TIMESTAMP    NOT NULL,
    end_time   TIMESTAMP    NOT NULL
)
