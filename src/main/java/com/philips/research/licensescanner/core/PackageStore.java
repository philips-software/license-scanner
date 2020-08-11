package com.philips.research.licensescanner.core;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;

import java.net.URI;
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
    Optional<Package> findPackage(String namespace, String name, String version);

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
}
