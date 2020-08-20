package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Detection;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.License;

import javax.persistence.*;
import java.net.URI;
import java.time.Instant;

/**
 * JPA entity for persisting a scan.
 */
@Entity
@Table(name = "scans")
class ScanEntity extends Scan {
    @Column(name = "created")
    private final Instant timestamp;

    @Id
    @GeneratedValue
    private Long id;

    public ScanEntity() {
        this(null, null, null);
    }

    ScanEntity(Instant timestamp, PackageEntity pkg, URI location) {
        super(pkg, location);
        this.timestamp = timestamp;
    }

    @Override
    protected Detection newDetection(License license) {
        final var detection = new DetectionEntity(this, license);
        PackageDatabase.detectionRepository.save(detection);
        return detection;
    }
}
