/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core;

import com.philips.research.licensescanner.core.domain.Scan;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistence API for packages.
 */
public interface ScanStore {
    /**
     * Creates a new persistent scan registration.
     *
     * @return scan instance
     */
    Scan createScan(URI purl, @NullOr URI location);

    /**
     * Finds the latest scan for a package.
     *
     * @param purl the package of the scan
     * @return the scan (if any)
     */
    Optional<Scan> getScan(URI purl);

    /**
     * Finds all packages (partly) containing the indicated mask.
     *
     * @return all matching packages
     */
    List<Scan> findScans(String namespace, String name, String version);

    /**
     * Removes a scan registration.
     */
    void deleteScan(Scan scan);

    /**
     * @return all scanning errors
     */
    List<Scan> scanErrors();

    /**
     * @return all contested scans
     */
    List<Scan> contested();

    /**
     * Finds all latest scans in a period.
     */
    List<Scan> findScans(Instant from, Instant until);

    /**
     * @return total number of successful license scans
     */
    int countLicenses();

    /**
     * @return total number of scanning errors
     */
    int countErrors();

    /**
     * @return total number of contested licenses
     */
    int countContested();
}
