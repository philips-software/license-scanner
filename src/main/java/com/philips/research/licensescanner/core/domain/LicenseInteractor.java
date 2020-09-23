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
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<LicenseInfo> licenseFor(String namespace, String name, String version) {
        return store.getPackage(namespace, name, version)
                .flatMap(store::latestScan)
                .map(this::toLicenseInfoWithDetections);
    }

    @Override
    @Async("licenseDetectionExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scanLicense(String namespace, String name, String version, URI location) {
        @NullOr Path path = null;
        @NullOr Scan scan = null;
        try {
            LOG.info("Scan license for {}:{} {} from {}", namespace, name, version, location);
            final var pkg = getOrCreatePackage(namespace, name, version);
            if (store.latestScan(pkg).isEmpty()) {
                scan = store.createScan(pkg, location);
                //TODO Check hash after download
                path = downloader.download(location);
                detector.scan(path, scan, licenseThreshold);
                LOG.info("Detected license for {}:{} {} is '{}'", namespace, name, version, scan.getLicense());
            }
        } catch (Exception e) {
            LOG.error("Scanning failed: " + e.toString());
            if (scan != null) {
                scan.setError(e.getMessage());
            }
        } finally {
            if (path != null) {
                deleteDirectory(path);
            }
        }
    }

    @Override
    public Optional<LicenseInfo> getScan(UUID scanId) {
        return store.getScan(scanId).map(this::toLicenseInfoWithDetections);
    }

    @Override
    public List<LicenseInfo> findScans(Instant from, Instant until) {
        final var results = store.findScans(from, until);
        return results.stream()
                .map(this::toLicenseInfo)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteScans(String namespace, String name, String version) {
        store.getPackage(namespace, name, version)
                .ifPresent(store::deleteScans);
    }

    private Package getOrCreatePackage(String origin, String name, String version) {
        return store.getPackage(origin, name, version).orElseGet(() -> store.createPackage(origin, name, version));
    }

    private void deleteDirectory(Path path) {
        try {
            LOG.info("Removing working directory {}", path);
            FileSystemUtils.deleteRecursively(path);
        } catch (IOException e) {
            LOG.warn("Failed to (fully) remove directory {}", path);
        }
    }

    private PackageId toPackageId(Package pkg) {
        final var id = new PackageId();
        id.namespace = pkg.getNamespace();
        id.name = pkg.getName();
        id.version = pkg.getVersion();
        return id;
    }

    private LicenseInfo toLicenseInfo(Scan scan) {
        final var info = new LicenseInfo();
        info.uuid = scan.getUuid();
        info.timestamp = scan.getTimestamp();
        info.pkg = toPackageId(scan.getPackage());
        info.license = scan.getLicense().toString();
        info.location = scan.getLocation().orElse(null);
        info.error = scan.getError().orElse(null);
        info.isContested = scan.isContested();
        info.isConfirmed = scan.isConfirmed();
        return info;
    }

    private LicenseInfo toLicenseInfoWithDetections(Scan scan) {
        final var info = toLicenseInfo(scan);
        info.detections = scan.getDetections().stream()
                .map(this::toDetectionInfo)
                .collect(Collectors.toList());
        return info;
    }

    private DetectionInfo toDetectionInfo(Detection detection) {
        final var info = new DetectionInfo();
        info.license = detection.getLicense().toString();
        info.file = detection.getFilePath().toString();
        info.startLine = detection.getStartLine();
        info.endLine = detection.getEndLine();
        info.confirmations = detection.getConfirmations();
        return info;
    }
}
