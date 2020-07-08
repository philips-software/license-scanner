package com.philips.research.licensescanner.core;

import java.util.Optional;

/**
 * License related use-cases.
 */
public interface LicenseService {
    /**
     * Provides license per package.
     *
     * @param packageId Package identifier
     * @param version   Package version name
     * @param vcsId     Version control coordinates
     * @return (Possibly emtpy) SPDX license string if package is known
     */
    Optional<String> licenseFor(String packageId, String version, String vcsId);

    /**
     * Queues package for scanning.
     *
     * @param packageId Package identifier
     * @param version   Package version name
     * @param vcsId     Version control coordinates
     */
    void scanLicense(String packageId, String version, String vcsId);

    /**
     * @return All current scanning errors.
     */
    Iterable<ErrorReport> scanErrors();

    class ErrorReport {
        public final String packageId;
        public final String version;
        public final String message;

        ErrorReport(String packageId, String version, String message) {
            this.packageId = packageId;
            this.version = version;
            this.message = message;
        }
    }
}


