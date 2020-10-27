ALTER TABLE packages
    ADD COLUMN purl CLOB;

UPDATE packages
SET namespace='/'
where namespace = '';

UPDATE packages
SET namespace=REPLACE(namespace, '@', '%40');

UPDATE packages
SET purl=CONCAT(namespace, '/', name, '@', version);

UPDATE packages
SET purl=REPLACE(purl, '//', '');

ALTER TABLE packages
    ALTER COLUMN purl CLOB NOT NULL;

ALTER TABLE packages
    DROP COLUMN namespace;
ALTER TABLE packages
    DROP COLUMN name;
ALTER TABLE packages
    DROP COLUMN version;
