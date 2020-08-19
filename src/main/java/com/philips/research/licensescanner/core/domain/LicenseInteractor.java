package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final int licenseThreshold;

    @Autowired
    public LicenseInteractor(PackageStore store, Downloader downloader, Detector detector,
                             @Value("${licenses.threshold-percent}") int licenseThreshold) {
        this.store = store;
        this.downloader = downloader;
        this.detector = detector;
        this.licenseThreshold = licenseThreshold;
    }

    @Override
    public List<PackageId> findPackages(String namespace, String name, String version) {
        final var packages = store.findPackages(namespace, name, version);
        return packages.stream().map(this::toPackageId).collect(Collectors.toList());
    }

    @Override
    public Optional<LicenseInfo> licenseFor(String origin, String name, String version) {
        return store.getPackage(origin, name, version)
                .flatMap(store::latestScan)
                .map(this::toLicenseInfo);
    }

    @Override
    @Async("licenseDetectionExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scanLicense(String origin, String name, String version, URI location) {
        Path path = null;
        Package pkg = null;
        try {
            LOG.info("Scan license for {}:{} {} from {}", origin, name, version, location);
            pkg = getOrCreatePackage(origin, name, version);
            path = downloader.download(location);
            //TODO Check hash after download
            final var copyright = detector.scan(path, licenseThreshold);
            store.createScan(pkg, copyright.getLicense().toString(), location);
            LOG.info("Detected license for {}:{} {} is '{}'", origin, name, version, copyright.getLicense());
        } catch (Exception e) {
            LOG.error("Scanning failed", e);
            if (pkg != null) {
                store.registerScanError(pkg, location, e.getMessage());
            }
        } finally {
            deleteDirectory(path);
        }
    }

    @Override
    public List<LicenseInfo> findScans(Instant from, Instant until) {
        final var results = store.findScans(from, until);
        return results.stream()
                .map(this::toLicenseInfo)
                .collect(Collectors.toList());
    }

    private Package getOrCreatePackage(String origin, String name, String version) {
        return store.getPackage(origin, name, version).orElseGet(() -> store.createPackage(origin, name, version));
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

    private PackageId toPackageId(Package pkg) {
        return new PackageId(pkg.getNamespace(), pkg.getName(), pkg.getVersion());
    }

    private LicenseInfo toLicenseInfo(Scan scan) {
        final var license = scan.getLicense().orElse(null);
        final var location = scan.getLocation().orElse(null);
        final var pkg = scan.getPackage();
        return new LicenseInfo(pkg.getNamespace(), pkg.getName(), pkg.getVersion(), location, license);
    }

}
