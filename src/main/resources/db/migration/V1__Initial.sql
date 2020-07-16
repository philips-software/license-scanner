CREATE TABLE packages
(
    id        BIGINT      NOT NULL,
    namespace VARCHAR(10) NOT NULL,
    name      VARCHAR(50) NOT NULL,
    version   VARCHAR(50) NOT NULL
);

CREATE TABLE scans
(
    id         BIGINT       NOT NULL,
    package_id BIGINT       NOT NULL,
    created    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    location   VARCHAR(500) NOT NULL,
    license    VARCHAR(200),
    error      VARCHAR(500),
    FOREIGN KEY (package_id) REFERENCES packages (id)
);

CREATE SEQUENCE hibernate_sequence;
