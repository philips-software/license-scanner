/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

CREATE TABLE detections
(
    id            BIGINT   NOT NULL,
    scan_id       BIGINT   NOT NULL,
    license       CLOB     NOT NULL,
    score         SMALLINT NOT NULL,
    file_path     CLOB     NOT NULL,
    start_line    INT      NOT NULL,
    end_line      INT      NOT NULL,
    confirmations INT      NOT NULL
);
