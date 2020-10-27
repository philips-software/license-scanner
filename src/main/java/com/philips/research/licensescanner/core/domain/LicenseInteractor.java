/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import com.philips.research.licensescanner.core.domain.license.License;
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
    public List<URI> findPackages(String namespace, String name, String version) {
        return store.findPackages(namespace, name, version).stream()
                .map(Package::getPurl)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LicenseDto> licenseFor(URI purl) {
        return store.getPackage(purl)
                .flatMap(store::latestScan)
                .map(DtoConverter::toDto);
    }

    @Override
    @Async("licenseDetectionExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scanLicense(URI purl, @NullOr URI location) {
        @NullOr Path path = null;
        @NullOr Scan scan = null;
        try {
            LOG.info("Scan license for {} from {}", purl, location);
            final var pkg = getOrCreatePackage(purl);
            if (store.latestScan(pkg).isEmpty()) {
                scan = store.createScan(pkg, location);
                if (location != null) {
                    //TODO Check hash after download
                    path = downloader.download(location);
                    detector.scan(path, scan, licenseThreshold);
                    LOG.info("Detected license for {} is '{}'", purl, scan.getLicense());
                } else {
                    scan.setError("No location provided");
                }
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
    public Optional<LicenseDto> getScan(UUID scanId) {
        return store.getScan(scanId).map(DtoConverter::toDto);
    }

    @Override
    public List<LicenseDto> findScans(Instant from, Instant until) {
        return store.findScans(from, until).stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LicenseDto> findErrors() {
        return store.scanErrors().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LicenseDto> findContested() {
        return store.contested().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void contest(UUID scanId) {
        store.getScan(scanId).ifPresent(Scan::contest);
    }

    @Override
    public void curateLicense(UUID scanId, @NullOr String license) {
        store.getScan(scanId)
                .ifPresent(s -> s.confirm((license != null) ? License.of(license) : s.getLicense()));
    }

    @Override
    public void deleteScans(URI purl) {
        store.getPackage(purl)
                .ifPresent(store::deleteScans);
    }

    private Package getOrCreatePackage(URI purl) {
        return store.getPackage(purl).orElseGet(() -> store.createPackage(purl));
    }

    private void deleteDirectory(Path path) {
        try {
            LOG.info("Removing working directory {}", path);
            FileSystemUtils.deleteRecursively(path);
        } catch (IOException e) {
            LOG.warn("Failed to (fully) remove directory {}", path);
        }
    }
}
