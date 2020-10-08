/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

CREATE TABLE packages
(
    id        BIGINT       NOT NULL,
    namespace VARCHAR(200) NOT NULL,
    name      VARCHAR(200) NOT NULL,
    version   VARCHAR(50)  NOT NULL
);

CREATE TABLE scans
(
    id         BIGINT    NOT NULL,
    package_id BIGINT    NOT NULL,
    created    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    location   CLOB      NOT NULL,
    license    CLOB,
    error      CLOB,
    FOREIGN KEY (package_id) REFERENCES packages (id)
);

CREATE SEQUENCE hibernate_sequence;
