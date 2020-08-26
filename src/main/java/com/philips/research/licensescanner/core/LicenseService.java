package com.philips.research.licensescanner.core;

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
     * Provides license per package.
     *
     * @param origin  Package manager
     * @param name    Package identifier
     * @param version Package version name
     * @return License information if the package is known.
     */
    Optional<LicenseInfo> licenseFor(String origin, String name, String version);

    /**
     * Queues package for scanning.
     *
     * @param origin  Package manager
     * @param name    Package identifier
     * @param version Package version name
     * @param vcsId   Version control coordinates
     */
    void scanLicense(String origin, String name, String version, URI vcsId);

    /**
     * Lists all latest scan results for the indicated period.
     *
     * @param from
     * @param until
     * @return scan results
     */
    List<LicenseInfo> findScans(Instant from, Instant until);

    /**
     * Response model for a package identifier.
     */
    class PackageId {
        public String namespace;
        public String name;
        public String version;
    }

    /**
     * Response model for license information.
     */
    class LicenseInfo {
        public UUID uuid;
        public Instant timestamp;
        public PackageId pkg;
        public URI location;
        public String license;
        public String error;
        public List<DetectionInfo> detections;
        public boolean isContested;
        public boolean isConfirmed;
    }

    /**
     * Response model for license detection information.
     */
    class DetectionInfo {
        public String license;
        public String file;
        public int startLine;
        public int endLine;
        public int confirmations;
    }
}

