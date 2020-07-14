package com.philips.research.licensescanner.core.domain;

import java.net.URI;
import java.util.Optional;

/**
 * Result of a license scan for a package.
 */
public class Scan {
    private final Package pkg;
    private final String license;
    private final URI location;

    public Scan(Package pkg, String license, URI location) {
        this.pkg = pkg;
        this.license = license;
        this.location = location;
    }

    public Package getPackage() {
        return pkg;
    }

    public Optional<String> getLicense() {
        return Optional.ofNullable(license);
    }

    public Optional<URI> getVcsUri() {
        return Optional.ofNullable(location);
    }
}
