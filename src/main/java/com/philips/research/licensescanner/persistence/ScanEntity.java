package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Scan;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URI;

@Entity(name = "scans")
class ScanEntity extends Scan {
    @Id
    @GeneratedValue
    private Long id;

    public ScanEntity() {
        this(null, null, null);
    }

    ScanEntity(PackageEntity pkg, String license, URI location) {
        super(pkg, license, location);
    }
}
