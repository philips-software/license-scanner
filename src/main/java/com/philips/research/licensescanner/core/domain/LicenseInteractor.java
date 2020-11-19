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
import com.philips.research.licensescanner.core.domain.download.DownloadCache;
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
    private final DownloadCache cache;
    private final Detector detector;
    private final ApplicationConfiguration configuration;

    @Autowired
    public LicenseInteractor(PackageStore store, DownloadCache cache, Detector detector,
                             ApplicationConfiguration configuration) {
        this.store = store;
        this.cache = cache;
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
        @NullOr Path path = null;
        final var purl = scan.getPackage().getPurl();
        try {
            path = cache.obtain(location);
            final var fragment = location.getFragment();
            if (fragment != null) {
                path = path.resolve(fragment);
            }
            detector.scan(path, scan, configuration.getThresholdPercent());
        } finally {
            cache.release(location);
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

    private void ignoreDetection(UUID scanId, String license, boolean ignored) {
        final var lic = LicenseParser.parse(license);
        getDetection(scanId, lic)
                .ifPresent(d -> {
                    d.setIgnored(ignored);
                    LOG.info("Scan {}: license {} is now {}", scanId, license, ignored ? "ignored" : "included");
                });
    }

    @Override
    public Optional<FileFragmentDto> sourceFragment(UUID scanId, String license, int margin) {
        return store.getScan(scanId)
                .flatMap(scan -> scan.getDetection(LicenseParser.parse(license))
                        .flatMap(det -> scan.getLocation()
                                .flatMap(location -> {
                                    final var dto = new FileFragmentDto();
                                    dto.filename = det.getFilePath().toString();
                                    final var offset = Math.max(0, det.getStartLine() - margin - 1);
                                    dto.firstLine = offset + 1;
                                    dto.focusStart = det.getStartLine() - offset - 1;
                                    dto.focusEnd = det.getEndLine() - offset;
                                    try {
                                        final var path = cache.obtain(location).resolve(dto.filename);
                                        dto.lines = Files.lines(path)
                                                .skip(offset)
                                                .limit(Math.min(margin, det.getStartLine() - 1) + det.getLineCount() + margin)
                                                .collect(Collectors.toList());
                                    } catch (IOException e) {
                                        throw new IllegalStateException("Could not load detection file " + dto.filename + " from " + location, e);
                                    }
                                    return Optional.of(dto);
                                })
                        )
                );
    }

    @Override
    public StatisticsDto statistics() {
        final var dto = new StatisticsDto();
        dto.licenses = store.countLicenses();
        dto.contested = store.countContested();
        dto.errors = store.countErrors();
        return dto;
    }

    private Package getOrCreatePackage(URI purl) {
        return store.getPackage(purl).orElseGet(() -> store.createPackage(purl));
    }

    private Optional<Detection> getDetection(UUID scanId, License lic) {
        return store.getScan(scanId)
                .flatMap(s -> s.getDetection(lic));
    }
}
