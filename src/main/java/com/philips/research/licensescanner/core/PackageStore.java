package com.philips.research.licensescanner.core;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;

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
     * Creates a new persistent package.
     *
     * @param namespace
     * @param name
     * @param version
     * @return package instance
     */
    Package createPackage(String namespace, String name, String version);

    /**
     * Finds a single package.
     *
     * @param namespace
     * @param name
     * @param version
     * @return the package (if any)
     */
    Optional<Package> getPackage(String namespace, String name, String version);

    /**
     * Finds all packages (partly) containing the indicated fields.
     *
     * @param namespace (frogment of) namespace
     * @param name      (fragment of) name
     * @param version   (fragment of) version
     * @return all matching packages
     */
    List<Package> findPackages(String namespace, String name, String version);

    /**
     * Deletes all scans for the indicated package.
     *
     * @param pkg
     */
    void deleteScans(Package pkg);

    /**
     * Creates a new persistent scan registration.
     *
     * @param pkg
     * @param location
     * @return scan instance
     */
    Scan createScan(Package pkg, URI location);

    /**
     * Finds the latest scan for a package.
     *
     * @param pkg the package of the scan
     * @return the scan (if any)
     */
    Optional<Scan> latestScan(Package pkg);

    /**
     * Removes a scan registration.
     *
     * @param scan
     */
    void deleteScan(Scan scan);

    /**
     * Finds all scanning errors for a package.
     *
     * @param pkg
     * @return all scanning errors, sorted on descending timestamp
     */
    List<Scan> scanErrors(Package pkg);

    /**
     * @return the requested scan
     */
    Optional<Scan> getScan(UUID scanId);

    /**
     * Finds all latest scans in a period.
     *
     * @param from
     * @param until
     */
    List<Scan> findScans(Instant from, Instant until);
}
