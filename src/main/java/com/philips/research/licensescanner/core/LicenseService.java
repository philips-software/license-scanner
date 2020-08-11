package com.philips.research.licensescanner.core;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * License related use-cases.
 */
public interface LicenseService {
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
     * @return All current scanning errors.
     */
    Iterable<ErrorReport> scanErrors();

    /**
     * Response model for license information.
     */
    class LicenseInfo {
        public final URI location;
        public final List<String> licenses;

        public LicenseInfo(URI location, List<String> licenses) {
            this.licenses = licenses;
            this.location = location;
        }
    }

    /**
     * Response model for error information.
     */
    class ErrorReport {
        public final String packageId;
        public final String version;
        public final String message;

        public ErrorReport(String name, String version, String message) {
            this.packageId = name;
            this.version = version;
            this.message = message;
        }
    }
}

