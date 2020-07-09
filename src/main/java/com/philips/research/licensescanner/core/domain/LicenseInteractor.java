package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.download.VcsUri;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

/**
 * License detection use cases implementation.
 */
@Service
public class LicenseInteractor implements LicenseService {
    private static final Logger LOG = LoggerFactory.getLogger(LicenseInteractor.class);

    private final Downloader downloader;
    private final Detector detector;

    @Autowired
    public LicenseInteractor(Downloader downloader, Detector detector) {
        this.downloader = downloader;
        this.detector = detector;
    }

    @Override
    public Optional<LicenseInfo> licenseFor(String origin, String pkg, String version) {
        return Optional.empty();
    }

    @Override
    public void scanLicense(String origin, String packageId, String version, URI vcsUri) {
        LOG.info("Scan license for {}:{} {} from {}", origin, packageId, version, vcsUri);
        final var location = VcsUri.from(vcsUri);
        final var path = downloader.download(location);
        final var copyright = detector.scan(path);
        deleteDirectory(path);
        LOG.info("Detected license for {}:{} {} is {}", origin, packageId, version, copyright.license);
    }

    private void deleteDirectory(Path path) {
        try {
            FileSystemUtils.deleteRecursively(path);
        } catch (IOException e) {
            LOG.warn("Failed to (fully) remove directory {}", path);
        }
    }

    @Override
    public Iterable<ErrorReport> scanErrors() {
        return null;
    }
}
