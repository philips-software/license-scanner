UPDATE scans SET license=null WHERE confirmed=false;

ALTER TABLE scans
    DROP COLUMN confirmed;
