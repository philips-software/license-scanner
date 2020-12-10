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
import com.philips.research.licensescanner.core.BusinessException;
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
            final var pkg = getOrCreatePackage(purl);
            if (store.latestScan(pkg).isEmpty()) {
                scan = store.createScan(pkg, location);
                if (location != null && location.getScheme() != null) {
                    scanPackage(location, scan);
                    LOG.info("Detected license for {} is '{}'", purl, scan.getLicense());
                } else {
                    scan.setError("No location provided");
                    LOG.info("No location provided for {}", purl);
                }
            }
        } catch (BusinessException e) {
            LOG.warn("Scanning failed: {}", e.getMessage());
            if (scan != null) {
                scan.setError(e.getMessage());
            }
        } catch (Exception e) {
            LOG.error("Scanning failed:", e);
            if (scan != null) {
                scan.setError("Server failure");
            }
        }
    }

    private void scanPackage(URI location, Scan scan) {
        try {
            LOG.info("Scan {} from {}", scan.getPackage().getPurl(), location);
            final var sourcePath = cache.obtain(location);
            final var path = resolveFragment(sourcePath, location.getFragment());
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
    public void deletePackage(URI purl) {
        store.getPackage(purl)
                .ifPresent(store::deletePackage);
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
        store.getScan(scanId).ifPresent(scan -> {
            scan.ignore(lic, ignored);
            LOG.info("Scan {}: license {} is now {}", scanId, license, ignored ? "ignored" : "included");
        });
    }

    @Override
    public Optional<FileFragmentDto> sourceFragment(UUID scanId, String license, int margin) {
        return store.getScan(scanId)
                .flatMap(scan -> scan.getDetection(LicenseParser.parse(license))
                        .flatMap(det -> scan.getLocation()
                                .flatMap(location -> fileFragmentDto(location, det, margin))
                        )
                );
    }

    private Optional<FileFragmentDto> fileFragmentDto(URI location, Detection det, int margin) {
        final var dto = new FileFragmentDto();
        dto.filename = det.getFilePath().toString();
        final var offset = Math.max(0, det.getStartLine() - margin - 1);
        dto.firstLine = offset + 1;
        dto.focusStart = det.getStartLine() - offset - 1;
        dto.focusEnd = det.getEndLine() - offset;
        try {
            var path = cache.obtain(location);
            path = resolveFragment(path, location.getFragment()).resolve(dto.filename);
            dto.lines = Files.lines(path)
                    .skip(offset)
                    .limit(Math.min(margin, det.getStartLine() - 1) + det.getLineCount() + margin)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load detection file " + dto.filename + " from " + location, e);
        }
        return Optional.of(dto);
    }

    private Path resolveFragment(Path path, @NullOr String fragment) {
        if (fragment != null) {
            path = path.resolve(fragment);
        }
        if (!path.toFile().exists()) {
            throw new LicenseException("Path '" + fragment + "' was not found in the source code");
        }
        return path;
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

}
