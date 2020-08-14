package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.ScanError;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;
import java.time.Instant;

/**
 * JPA entity for persisting a scan.
 */
@Entity
@Table(name="scans")
class ScanErrorEntity extends ScanError {
    @Id
    @GeneratedValue
    private Long id;

    public ScanErrorEntity() {
        this(null, null, null, null);
    }

    ScanErrorEntity(Instant timestamp, PackageEntity pkg, URI location, String message) {
        super(timestamp, pkg, location, message);
    }
}
