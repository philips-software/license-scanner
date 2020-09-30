/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.DownloadException;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
import com.philips.research.licensescanner.core.domain.license.DetectorException;
import com.philips.research.licensescanner.core.domain.license.License;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LicenseInteractorTest {
    private static final String ORIGIN = "Origin";
    private static final String NAME = "Package";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";
    private static final URI LOCATION = URI.create("git+git://example.com");
    private static final Package PACKAGE = new Package(ORIGIN, NAME, VERSION);
    private static final Scan SCAN = new Scan(PACKAGE, LOCATION)
            .addDetection(License.of(LICENSE), 73, new File(""), 1, 2);
    private static final UUID SCAN_ID = SCAN.getUuid();
    private static final Instant UNTIL = Instant.now();
    private static final Instant FROM = UNTIL.minus(Duration.ofDays(5));
    private static final int THRESHOLD = 70;

    private final Downloader downloader = mock(Downloader.class);
    private final Detector detector = mock(Detector.class);
    private final PackageStore store = mock(PackageStore.class);

    private final LicenseService interactor = new LicenseInteractor(store, downloader, detector, THRESHOLD);

    @BeforeEach
    void beforeEach() {
        when(store.getPackage(ORIGIN, NAME, VERSION)).thenReturn(Optional.of(PACKAGE));
    }

    @Nested
    class FindPackages {
        @Test
        void findsPackages() {
            when(store.findPackages(ORIGIN, NAME, VERSION)).thenReturn(List.of(new Package(ORIGIN, NAME, VERSION)));

            final var result = interactor.findPackages(ORIGIN, NAME, VERSION);

            assertThat(result).hasSize(1);
            final var pkg = result.get(0);
            assertThat(pkg.namespace).isEqualTo(ORIGIN);
            assertThat(pkg.name).isEqualTo(NAME);
            assertThat(pkg.version).isEqualTo(VERSION);
        }
    }

    @Nested
    class QueryLicenseInformation {
        @Test
        void noLicense_packageNotFound() {
            when(store.getPackage(ORIGIN, NAME, VERSION)).thenReturn(Optional.empty());

            final var noInfo = interactor.licenseFor(ORIGIN, NAME, VERSION);

            assertThat(noInfo).isEmpty();
        }

        @Test
        void noLicense_noScanForPackage() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.empty());

            final var noInfo = interactor.licenseFor(ORIGIN, NAME, VERSION);

            assertThat(noInfo).isEmpty();
        }

        @Test
        void retrievesLicenseForPackage() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.of(SCAN));

            @SuppressWarnings("OptionalGetWithoutIsPresent") final var info = interactor.licenseFor(ORIGIN, NAME, VERSION).get();

            assertThat(info.license).contains(LICENSE);
            assertThat(info.location).isEqualTo(LOCATION);
        }
    }

    @Nested
    class PackageScanning {
        private final Scan scan = new Scan(PACKAGE, LOCATION);

        Path directory;

        @BeforeEach
        void beforeEach() throws Exception {
            directory = Files.createTempDirectory("test");
            when(store.createScan(PACKAGE, LOCATION)).thenReturn(scan);
        }

        @AfterEach
        void afterEach() throws Exception {
            FileSystemUtils.deleteRecursively(directory);
        }

        @Test
        void skipsIfAlreadyScanned() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.of(scan));

            interactor.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(store, never()).createScan(PACKAGE, LOCATION);
            verify(detector, never()).scan(any(Path.class), any(Scan.class), anyInt());
        }

        @Test
        void skipsIfNoLocation() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.empty());
            when(store.createScan(PACKAGE, null)).thenReturn(scan);

            interactor.scanLicense(ORIGIN, NAME, VERSION, null);

            assertThat(scan.getError()).isNotEmpty();
            verify(detector, never()).scan(any(Path.class), any(Scan.class), anyInt());
        }

        @Test
        void downloadsAndScansPackage() {
            when(downloader.download(LOCATION)).thenReturn(directory);

            interactor.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(detector).scan(directory, scan, THRESHOLD);
            assertThat(directory.toFile()).doesNotExist();
        }

        @Test
        void registersDownloadFailure() {
            final var message = "Test error";
            when(downloader.download(LOCATION)).thenThrow(new DownloadException(message));

            interactor.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            assertThat(scan.getError()).contains(message);
        }

        @Test
        void registersScanningFailure() {
            final var message = "Test error";
            when(downloader.download(LOCATION)).thenReturn(directory);
            doThrow(new DetectorException(message, null)).when(detector).scan(directory, scan, THRESHOLD);

            interactor.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            assertThat(scan.getError()).contains(message);
            assertThat(directory.toFile()).doesNotExist();
        }
    }

    @Nested
    class QueryScanResults {
        @Test
        void findsScanByUuid() {
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(SCAN));

            final var result = interactor.getScan(SCAN_ID);

            assertThat(result).isPresent();
            assertThat(result.get().detections).isNotEmpty();
        }

        @Test
        void findsScansForPeriod() {
            when(store.findScans(FROM, UNTIL)).thenReturn(List.of(new Scan(PACKAGE, LOCATION)
                    .addDetection(License.of(LICENSE), 100, null, 1, 2)));

            final var result = interactor.findScans(FROM, UNTIL);

            assertThat(result).hasSize(1);
            final var pkg = result.get(0);
            assertThat(pkg.pkg.name).isEqualTo(NAME);
            assertThat(pkg.license).contains(LICENSE);
            assertThat(pkg.location).isEqualTo(LOCATION);
        }

        @Test
        void contestsScan() {
            final var scan = new Scan(PACKAGE, null);
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(scan));

            interactor.contest(SCAN_ID);

            assertThat(scan.isContested()).isTrue();
        }

        @Test
        void deletesScansForPackage() {
            interactor.deleteScans(ORIGIN, NAME, VERSION);

            verify(store).deleteScans(PACKAGE);
        }
    }
}
