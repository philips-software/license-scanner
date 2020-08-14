package com.philips.research.licensescanner.core;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.ScanError;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
     * Creates a new persistent scan registration.
     *
     * @param pkg
     * @param license
     * @param location
     * @return scan instance
     */
    Scan createScan(Package pkg, String license, URI location);

    /**
     * Finds the latest scan for a package.
     *
     * @param pkg the package of the scan
     * @return the scan (if any)
     */
    Optional<Scan> latestScan(Package pkg);

    /**
     * Registers a failed package scan.
     *
     * @param pkg
     * @param location
     * @param message
     */
    void registerScanError(Package pkg, URI location, String message);

    /**
     * Finds all scanning errors for a package.
     *
     * @param pkg
     * @return all scanning errors, sorted on descending timestamp
     */
    List<ScanError> scanErrors(Package pkg);

    /**
     * Finds all latest scans in a period.
     *
     * @param from
     * @param until
     */
    List<Scan> findScans(Instant from, Instant until);
}
