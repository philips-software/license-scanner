package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * License detection use cases implementation.
 */
@Service
@Transactional
public class LicenseInteractor implements LicenseService {
    private static final Logger LOG = LoggerFactory.getLogger(LicenseInteractor.class);

    private final PackageStore store;
    private final Downloader downloader;
    private final Detector detector;

    @Autowired
    public LicenseInteractor(PackageStore store, Downloader downloader, Detector detector) {
        this.store = store;
        this.downloader = downloader;
        this.detector = detector;
    }

    @Override
    public Optional<LicenseInfo> licenseFor(String origin, String name, String version) {
        return store.findPackage(origin, name, version)
                .flatMap(store::latestScan)
                .map(this::toLicenseInfo);
    }

    private LicenseInfo toLicenseInfo(Scan scan) {
        final var licenses = scan.getLicense().map(List::of).orElse(List.of());
        final var location = scan.getLocation().orElse(null);
        return new LicenseInfo(location, licenses);
    }

    @Override
    @Async("licenseDetectionExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scanLicense(String origin, String name, String version, URI location) {
        Path path = null;
        try {
            LOG.info("Scan license for {}:{} {} from {}", origin, name, version, location);
            final var pkg = getOrCreatePackage(origin, name, version);
            path = downloader.download(location);
            //TODO Check hash after download
            final var copyright = detector.scan(path);
            store.createScan(pkg, copyright.license, location);
            LOG.info("Detected license for {}:{} {} is {}", origin, name, version, copyright.license);
        } catch (Exception e) {
            LOG.error("Scanning failed", e);
        } finally {
            deleteDirectory(path);
        }
    }

    private Package getOrCreatePackage(String origin, String name, String version) {
        return store.findPackage(origin, name, version).orElseGet(() -> store.createPackage(origin, name, version));
    }

    private void deleteDirectory(Path path) {
        try {
            if (path != null) {
                LOG.info("Removing working directory {}", path);
                FileSystemUtils.deleteRecursively(path);
            }
        } catch (IOException e) {
            LOG.warn("Failed to (fully) remove directory {}", path);
        }
    }

    @Override
    public Iterable<ErrorReport> scanErrors() {
        return null;
    }
}
