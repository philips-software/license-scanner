package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * License detection use cases implementation.
 */
@Service
public class LicenseInteractor implements LicenseService {
    private final Downloader downloader;
    private final Detector detector;

    @Autowired
    public LicenseInteractor(Downloader downloader, Detector detector) {
        this.downloader = downloader;
        this.detector = detector;
    }

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
