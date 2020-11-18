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

import com.philips.research.licensescanner.ApplicationConfiguration;
import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.DownloadException;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
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
    private final ApplicationConfiguration configuration;

    @Autowired
    public LicenseInteractor(PackageStore store, Downloader downloader, Detector detector,
                             ApplicationConfiguration configuration) {
        this.store = store;
        this.downloader = downloader;
        this.detector = detector;
        this.configuration = configuration;
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
        @NullOr Scan scan = null;
        try {
            LOG.info("Scan license for {} from {}", purl, (location != null) ? location : "(no location)");
            final var pkg = getOrCreatePackage(purl);
            if (store.latestScan(pkg).isEmpty()) {
                scan = store.createScan(pkg, location);
                if (location != null) {
                    scanPackage(location, scan);
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
        }
    }

    private void scanPackage(URI location, Scan scan) {
        @NullOr Path tempDir = null;
        try {
            tempDir = createWorkingDirectory();
            //TODO Check hash after download
            final var path = downloader.download(tempDir, location);
            detector.scan(path, scan, configuration.getThresholdPercent());
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private Path createWorkingDirectory() {
        try {
            return Files.createTempDirectory(configuration.getTempDir(), "license-");
        } catch (IOException e) {
            throw new DownloadException("Failed to create a working directory", e);
        }
    }

    private void deleteDirectory(@NullOr Path path) {
        if (path == null) {
            return;
        }

        try {
            LOG.info("Removing working directory {}", path);
            FileSystemUtils.deleteRecursively(path);
        } catch (IOException e) {
            LOG.warn("Failed to (fully) remove directory {}", path);
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
    public void contest(UUID scanId, @NullOr String license) {
        final var contesting = (license != null) ? LicenseParser.parse(license) : License.NONE;
        store.getScan(scanId).ifPresent(scan -> scan.contest(contesting));
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

    @Override
    public void ignore(UUID scanId, String license) {
        ignoreDetection(scanId, license, true);
    }

    @Override
    public void restore(UUID scanId, String license) {
        ignoreDetection(scanId, license, false);
    }

    @Override
    public StatisticsDto statistics() {
        final var dto = new StatisticsDto();
        dto.licenses = store.countLicenses();
        dto.contested = store.countContested();
        dto.errors = store.countErrors();
        return dto;
    }

    private void ignoreDetection(UUID scanId, String license, boolean ignored) {
        final var lic = LicenseParser.parse(license);
        store.getScan(scanId)
                .flatMap(s -> s.getDetection(lic))
                .ifPresent(d -> {
                    d.setIgnored(ignored);
                    LOG.info("Scan {}: license {} is now {}", scanId, license, ignored ? "ignored" : "included");
                });
    }

    private Package getOrCreatePackage(URI purl) {
        return store.getPackage(purl).orElseGet(() -> store.createPackage(purl));
    }

}
