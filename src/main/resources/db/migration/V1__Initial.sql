CREATE TABLE packages
(
    id      BIGINT      NOT NULL,
    origin  VARCHAR(10) NOT NULL,
    name    VARCHAR(50) NOT NULL,
    version VARCHAR(50) NOT NULL
);

CREATE TABLE scans
(
    id         BIGINT       NOT NULL,
    package_id BIGINT       NOT NULL,
    created    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    location   VARCHAR(255) NOT NULL,
    license    VARCHAR(255),
    error      VARCHAR(255),
    FOREIGN KEY (package_id) REFERENCES packages (id)
);

CREATE SEQUENCE hibernate_sequence;
