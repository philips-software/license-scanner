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
    ADD COLUMN purl CLOB;

UPDATE scans
SET scans.purl = CONCAT('pkg:', (SELECT purl FROM packages WHERE packages.id = scans.package_id));
ALTER TABLE scans
    ALTER COLUMN purl CLOB NOT NULL;

ALTER TABLE scans
    DROP COLUMN uuid;
ALTER TABLE scans
    DROP COLUMN package_id;
DROP TABLE packages;

