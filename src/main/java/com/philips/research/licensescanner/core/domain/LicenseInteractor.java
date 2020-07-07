package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;

import java.util.Optional;

public class LicenseInteractor implements LicenseService {
    @Override
    public Optional<String> licenseFor(String packageId, String version, String vcsId) {
        return Optional.empty();
    }

    @Override
    public void scanLicense(String packageId, String version, String vcsId) {

    }

    @Override
    public Iterable<ErrorReport> scanErrors() {
        return null;
    }
}
