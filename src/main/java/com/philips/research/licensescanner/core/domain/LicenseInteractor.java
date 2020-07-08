package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.domain.download.DownloadLocation;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
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
    public Optional<String> licenseFor(String packageId, String version, String vcsId) {
        return Optional.empty();
    }

    @Override
    public void scanLicense(String packageId, String version, String vcsId) {
        final var location = DownloadLocation.parse(vcsId);
        final var path = downloader.download(location);
        final var copyright = detector.scan(path);
        deleteDirectory(path);
        LOG.info("Detected license for {} {} at {} is {}", packageId, version, vcsId, copyright.license);
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
