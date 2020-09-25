package com.philips.research.licensescanner.core;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * License related use-cases.
 */
public interface LicenseService {
    /**
     * Finds all packages matching the provided parameters.
     *
     * @param namespace (optional) fraction of the namespace
     * @param name      (optional) fraction of the name
     * @param version   (optional) fraction of the version
     * @return list of matching packages
     */
    List<PackageId> findPackages(String namespace, String name, String version);

    /**
     * @return License information if the package is known.
     */
    Optional<LicenseInfo> licenseFor(String namespace, String name, String version);

    /**
     * Queues package for scanning.
     *
     * @param vcsId Version control coordinates
     */
    void scanLicense(String namespace, String name, String version, @NullOr URI vcsId);

    /**
     * @return the details for the indicated scan
     */
    Optional<LicenseInfo> getScan(UUID scanId);

    /**
     * Lists all latest scan results for the indicated period.
     *
     * @return scan results
     */
    List<LicenseInfo> findScans(Instant from, Instant until);

    /**
     * Clear any existing scans for the indicated package.
     */
    void deleteScans(String namespace, String name, String version);

    /**
     * Response model for a package identifier.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class PackageId {
        public String namespace;
        public String name;
        public String version;
    }

    /**
     * Response model for license information.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class LicenseInfo {
        public UUID uuid;
        public Instant timestamp;
        public PackageId pkg;
        public String license;
        public @NullOr URI location;
        public @NullOr String error;
        public @NullOr List<DetectionInfo> detections;
        public boolean isContested;
        public boolean isConfirmed;
    }

    /**
     * Response model for license detection information.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class DetectionInfo {
        public String license;
        public String file;
        public int startLine;
        public int endLine;
        public int confirmations;
    }
}
