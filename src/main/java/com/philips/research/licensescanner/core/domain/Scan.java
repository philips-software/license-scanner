package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.download.VcsUri;

import java.util.Optional;

public class Scan {
    private final Package pkg;

    private String license;
    private VcsUri vcsUri;

    public Scan(Package pkg) {
        this.pkg = pkg;
    }

    public Package getPackage() {
        return pkg;
    }

    public Optional<String> getLicense() {
        return Optional.ofNullable(license);
    }

    public Scan setLicense(String license) {
        this.license = license;
        return this;
    }

    public Optional<VcsUri> getVcsUri() {
        return Optional.ofNullable(vcsUri);
    }

    public Scan setVcsUri(VcsUri vcsUri) {
        this.vcsUri = vcsUri;
        return this;
    }
}
