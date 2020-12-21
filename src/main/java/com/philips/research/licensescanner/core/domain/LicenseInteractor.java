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
import com.philips.research.licensescanner.core.ScanStore;
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
import java.util.stream.Collectors;

/**
 * License detection use cases implementation.
 */
@Service
@Transactional
public class LicenseInteractor implements LicenseService {
    private static final Logger LOG = LoggerFactory.getLogger(LicenseInteractor.class);

    private final ScanStore store;
    private final DownloadCache cache;
    private final Detector detector;
    private final ApplicationConfiguration configuration;

    @Autowired
    public LicenseInteractor(ScanStore store, DownloadCache cache, Detector detector,
                             ApplicationConfiguration configuration) {
        this.store = store;
        this.cache = cache;
        this.detector = detector;
        this.configuration = configuration;
    }

    @Override
    public List<ScanDto> findScans(String namespace, String name, String version) {
        return store.findScans(namespace, name, version).stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ScanDto> scanFor(URI purl) {
        return store.getScan(purl)
                .map(DtoConverter::toDto);
    }

    @Override
    @Async("licenseDetectionExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scanLicense(URI purl, @NullOr URI location) {
        @NullOr Scan scan = null;
        try {
            if (store.getScan(purl).isEmpty()) {
                scan = store.createScan(purl, location);
                if (location != null && !location.toString().isBlank()) {
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
            LOG.info("Scan {} from {}", scan.getPurl(), location);
            final var sourcePath = cache.obtain(location);
            final var path = resolveFragment(sourcePath, location.getFragment());
            detector.scan(path, scan, configuration.getThresholdPercent());
        } finally {
            cache.release(location);
        }
    }

    @Override
    public Optional<ScanDto> getScan(URI purl) {
        return store.getScan(purl).map(DtoConverter::toDto);
    }

    @Override
    public List<ScanDto> findScans(Instant from, Instant until) {
        return store.findScans(from, until).stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScanDto> findErrors() {
        return store.scanErrors().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScanDto> findContested() {
        return store.contested().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void contest(URI purl, @NullOr String license) {
        final var contesting = (license != null) ? LicenseParser.parse(license) : License.NONE;
        store.getScan(purl).ifPresent(scan -> {
            scan.contest(contesting);
            LOG.info("Contested {} with license {}", scan, license);
        });
    }

    @Override
    public void curateLicense(URI purl, @NullOr String license) {
        store.getScan(purl)
                .ifPresent(scan -> {
                    scan.confirm((license != null) ? License.of(license) : scan.getLicense());
                    LOG.info("Curated {} to have license {}", scan, license);
                });
    }

    @Override
    public void deleteScan(URI purl) {
        store.getScan(purl)
                .ifPresent(scan -> {
                    store.deleteScan(scan);
                    LOG.info("Deleted {}", scan);
                });
    }

    @Override
    public void ignore(URI purl, String license) {
        ignoreDetection(purl, license, true);
    }

    @Override
    public void restore(URI purl, String license) {
        ignoreDetection(purl, license, false);
    }

    private void ignoreDetection(URI purl, String license, boolean ignored) {
        final var lic = LicenseParser.parse(license);
        store.getScan(purl).ifPresent(scan -> {
            scan.ignore(lic, ignored);
            LOG.info("Scan {}: license {} is now {}", purl, license, ignored ? "ignored" : "included");
        });
    }

    @Override
    public Optional<FileFragmentDto> sourceFragment(URI purl, String license, int margin) {
        return store.getScan(purl)
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
        } catch (Exception e) {
            throw new LicenseException("Could not read file " + dto.filename);
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
}
