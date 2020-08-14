package com.philips.research.licensescanner.core;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
     * Lists all latest successful scan results for the indicated period.
     *
     * @param from
     * @param until
     * @return scan results
     */
    List<LicenseInfo> findScans(Instant from, Instant until);

    /**
     * @return All current scanning errors.
     */
    Iterable<ErrorReport> scanErrors();

    /**
     * Response model for a package identifier.
     */
    class PackageId {
        public final String namespace;
        public final String name;
        public final String version;

        public PackageId(String namespace, String name, String version) {
            this.namespace = namespace;
            this.name = name;
            this.version = version;
        }
    }

    /**
     * Response model for license information.
     */
    class LicenseInfo extends PackageId {
        public final URI location;
        public final List<String> licenses;

        public LicenseInfo(String namespace, String name, String version, URI location, List<String> licenses) {
            super(namespace, name, version);
            this.licenses = licenses;
            this.location = location;
        }
    }

    /**
     * Response model for error information.
     */
    class ErrorReport extends PackageId {
        public final Instant timestamp;
        public final String message;

        public ErrorReport(Instant timestamp, String namespace, String name, String version, String message) {
            super(namespace, name, version);
            this.timestamp = timestamp;
            this.message = message;
        }
    }
}

