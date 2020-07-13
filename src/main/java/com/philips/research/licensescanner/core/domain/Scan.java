package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.download.VcsUri;

import java.util.Optional;

/**
 * Result of a license scan for a package.
 */
public class Scan {
    private final Package pkg;
    private final String license;
    private final VcsUri vcsUri;

    public Scan(Package pkg, String license, VcsUri vcsUri) {
        this.pkg = pkg;
        this.license = license;
        this.vcsUri = vcsUri;
    }

    public Package getPackage() {
        return pkg;
    }

    public Optional<String> getLicense() {
        return Optional.ofNullable(license);
    }

    public Optional<VcsUri> getVcsUri() {
        return Optional.ofNullable(vcsUri);
    }
}
