package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.download.VcsUri;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "scans")
class ScanEntity extends Scan {
    @Id
    @GeneratedValue
    private long id;

    public ScanEntity() {
        this(null, null, null);
    }

    ScanEntity(Package pkg, String license, VcsUri vcsUri) {
        super(pkg, license, vcsUri);
    }
}
