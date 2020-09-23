package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Detection;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.License;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;

/**
 * JPA entity for persisting a scan.
 */
@Entity
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "scans")
class ScanEntity extends Scan {
    @Id
    @GeneratedValue
    @SuppressWarnings({"unused", "RedundantSuppression"})
    private @NullOr Long id;

    public ScanEntity() {
        //noinspection ConstantConditions
        this(null, null);
    }

    ScanEntity(PackageEntity pkg, URI location) {
        super(pkg, location);
    }

    @Override
    protected Detection newDetection(License license) {
        final var detection = new DetectionEntity(license);
        PackageDatabase.detectionRepository.save(detection);
        return detection;
    }
}
