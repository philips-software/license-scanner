/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

CREATE SEQUENCE hibernate_sequence START WITH 1;

CREATE TABLE scans
(
    id         BIGINT    NOT NULL,
    purl       CLOB      NOT NULL,
    location   CLOB,
    license    CLOB,
    contesting CLOB,
    error      CLOB,
    created    TIMESTAMP NOT NULL
);
ALTER TABLE scans
    ADD CONSTRAINT pk_scans PRIMARY KEY (id);

CREATE TABLE detections
(
    id            BIGINT  NOT NULL,
    scan_id       BIGINT,
    license       CLOB,
    file_path     CLOB,
    start_line    INTEGER NOT NULL,
    end_line      INTEGER NOT NULL,
    score         SMALLINT,
    confirmations INTEGER NOT NULL,
    ignored       BOOLEAN NOT NULL
);
ALTER TABLE detections
    ADD CONSTRAINT pk_detections PRIMARY KEY (id);

ALTER TABLE detections
    ADD CONSTRAINT fk_detections__scan_id FOREIGN KEY (scan_id) REFERENCES scans (id) NOCHECK;
