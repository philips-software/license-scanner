package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Scan;

import javax.persistence.*;
import java.net.URI;
import java.time.Instant;

/**
 * JPA entity for persisting a scan.
 */
@Entity
@Table(name = "scans")
class ScanEntity extends Scan {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "created")
    private final Instant timestamp;

    public ScanEntity() {
        this(null, null, null, null);
    }

    ScanEntity(Instant timestamp, PackageEntity pkg, String license, URI location) {
        super(pkg, license, location);
        this.timestamp = timestamp;
    }
}
