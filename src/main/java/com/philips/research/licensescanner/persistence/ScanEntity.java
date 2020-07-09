package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "scans")
public class ScanEntity extends Scan {
    @Id
    @GeneratedValue
    private long id;

    public ScanEntity() {
        this(null);
    }

    public ScanEntity(Package aPackage) {
        super(aPackage);
    }
}
