ALTER TABLE scans
    ADD COLUMN contesting CLOB;

UPDATE scans SET contesting = '' WHERE contested = true;

ALTER TABLE scans
    DROP COLUMN contested;
