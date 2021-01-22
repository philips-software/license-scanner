/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Detection;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.License;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.persistence.*;
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
    // Used for querying database on string match
    @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
    @Column(name = "purl", insertable = false, updatable = false)
    private String search;

    public ScanEntity() {
        //noinspection ConstantConditions
        this(null, null);
    }

    ScanEntity(URI purl, @NullOr URI location) {
        super(purl, location);
    }

    @Override
    protected Detection newDetection(License license) {
        final var detection = new DetectionEntity(license);
        PersistentDatabase.detectionRepository.save(detection);
        return detection;
    }
}
