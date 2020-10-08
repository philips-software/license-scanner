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
    ALTER COLUMN location CLOB NULL;
ALTER TABLE detections
    ALTER COLUMN scan_id BIGINT NULL;
