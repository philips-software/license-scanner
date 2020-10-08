/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

ALTER TABLE scans
    ADD COLUMN uuid UUID NOT NULL DEFAULT random_uuid();
ALTER TABLE scans
    ADD COLUMN contested BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE scans
    ADD COLUMN confirmed BOOLEAN NOT NULL DEFAULT FALSE;
