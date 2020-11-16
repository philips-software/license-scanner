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

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence API for packages.
 */
public interface PackageStore {
    /**
     * @return a new package instance
     */
    Package createPackage(URI purl);

    /**
     * Finds a single package.
     *
     * @return the package (if any)
     */
    Optional<Package> getPackage(URI purl);

    /**
     * Finds all packages (partly) containing the indicated mask.
     *
     * @return all matching packages
     */
    List<Package> findPackages(String namespace, String name, String version);

    /**
     * Deletes all scans for the indicated package.
     */
    void deleteScans(Package pkg);

    /**
     * Creates a new persistent scan registration.
     *
     * @return scan instance
     */
    Scan createScan(Package pkg, @NullOr URI location);

    /**
     * Finds the latest scan for a package.
     *
     * @param pkg the package of the scan
     * @return the scan (if any)
     */
    Optional<Scan> latestScan(Package pkg);

    /**
     * Removes a scan registration.
     */
    void deleteScan(Scan scan);

    /**
     * Finds all scanning errors for a package.
     *
     * @return all scanning errors, sorted on descending timestamp
     */
    List<Scan> scanErrors(Package pkg);

    /**
     * @return all scanning errors
     */
    List<Scan> scanErrors();

    /**
     * @return all contested scans
     */
    List<Scan> contested();

    /**
     * @return the requested scan
     */
    Optional<Scan> getScan(UUID scanId);

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
