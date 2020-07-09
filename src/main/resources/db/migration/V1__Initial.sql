CREATE TABLE packages
(
    id      INT         NOT NULL,
    origin  VARCHAR(10) NOT NULL,
    name    VARCHAR(50) NOT NULL,
    version VARCHAR(50) NOT NULL
);

CREATE TABLE scans
(
    package_id INT          NOT NULL,
    created    TIMESTAMP    NOT NULL,
    vcs_uri    VARCHAR(100) NOT NULL,
    license    VARCHAR(200),
    manual     BOOLEAN      NOT NULL,
    error      VARCHAR(500),
    FOREIGN KEY (package_id) REFERENCES packages (id)
);

