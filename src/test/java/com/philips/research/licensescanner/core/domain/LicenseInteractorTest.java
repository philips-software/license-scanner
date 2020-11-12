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
import com.philips.research.licensescanner.core.domain.license.DetectorException;
import com.philips.research.licensescanner.core.domain.license.License;
import org.junit.jupiter.api.*;
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
    private static final String OTHER = "Other";
    private static final String MESSAGE = "Test message";
    private static final URI LOCATION = URI.create("git+git://example.com");
    private static final File FILE = new File(".");
    private static final URI PURL = URI.create("pkg:package@version");
    private static final Package PACKAGE = new Package(PURL);
    private static final Scan SCAN = new Scan(PACKAGE, LOCATION)
            .addDetection(License.of(LICENSE), 73, new File(""), 1, 2);
    private static final UUID SCAN_ID = SCAN.getUuid();
    private static final Instant UNTIL = Instant.now();
    private static final Instant FROM = UNTIL.minus(Duration.ofDays(5));
    private static final int THRESHOLD = 70;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private static Path workDirectory;

    private final Downloader downloader = mock(Downloader.class);
    private final Detector detector = mock(Detector.class);
    private final PackageStore store = mock(PackageStore.class);
    private final ApplicationConfiguration configuration = new ApplicationConfiguration();

    private final LicenseService interactor = new LicenseInteractor(store, downloader, detector, configuration);

    @BeforeAll
    static void beforeAll() throws Exception {
        workDirectory = Files.createTempDirectory("test");
    }

    @AfterAll
    static void afterAll() throws Exception {
        FileSystemUtils.deleteRecursively(workDirectory);
    }

    @BeforeEach
    void beforeEach() {
        when(store.getPackage(PURL)).thenReturn(Optional.of(PACKAGE));
    }

    @Nested
    class FindPackages {
        @Test
        void findsPackages() {
            when(store.findPackages(ORIGIN, NAME, VERSION)).thenReturn(List.of(new Package(PURL)));

            final var result = interactor.findPackages(ORIGIN, NAME, VERSION);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(PURL);
        }
    }

    @Nested
    class QueryLicenseInformation {
        @Test
        void noLicense_packageNotFound() {
            when(store.getPackage(PURL)).thenReturn(Optional.empty());

            final var noInfo = interactor.licenseFor(PURL);

            assertThat(noInfo).isEmpty();
        }

        @Test
        void noLicense_noScanForPackage() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.empty());

            final var noInfo = interactor.licenseFor(PURL);

            assertThat(noInfo).isEmpty();
        }

        @Test
        void retrievesLicenseForPackage() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.of(SCAN));

            @SuppressWarnings("OptionalGetWithoutIsPresent") final var info = interactor.licenseFor(PURL).get();

            assertThat(info.license).contains(LICENSE);
            assertThat(info.location).isEqualTo(LOCATION);
        }
    }

    @Nested
    class PackageScanning {
        private final Scan scan = new Scan(PACKAGE, LOCATION);

        @BeforeEach
        void beforeEach() {
            when(store.createScan(PACKAGE, LOCATION)).thenReturn(scan);
            configuration.setTempDir(workDirectory);
            configuration.setThresholdPercent(THRESHOLD);
        }

        @Test
        void skipsIfAlreadyScanned() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.of(scan));

            interactor.scanLicense(PURL, LOCATION);

            verify(store, never()).createScan(PACKAGE, LOCATION);
            verify(detector, never()).scan(any(Path.class), any(Scan.class), anyInt());
        }

        @Test
        void skipsIfNoLocation() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.empty());
            when(store.createScan(PACKAGE, null)).thenReturn(scan);

            interactor.scanLicense(PURL, null);

            assertThat(scan.getError()).isNotEmpty();
            verify(detector, never()).scan(any(Path.class), any(Scan.class), anyInt());
        }

        @Test
        void downloadsAndScansPackage() {
            final var scanDir = workDirectory.resolve("subpath");
            when(downloader.download(any(Path.class), eq(LOCATION))).thenReturn(scanDir);

            interactor.scanLicense(PURL, LOCATION);

            verify(detector).scan(scanDir, scan, THRESHOLD);
        }

        @Test
        void registersDownloadFailure() {
            when(downloader.download(any(Path.class), eq(LOCATION))).thenThrow(new DownloadException(MESSAGE));

            interactor.scanLicense(PURL, LOCATION);

            assertThat(scan.getError()).contains(MESSAGE);
        }

        @Test
        void registersScanningFailure() {
            when(downloader.download(any(Path.class), eq(LOCATION))).thenReturn(workDirectory);
            doThrow(new DetectorException(MESSAGE, new IllegalArgumentException()))
                    .when(detector).scan(workDirectory, scan, THRESHOLD);

            interactor.scanLicense(PURL, LOCATION);

            assertThat(scan.getError()).contains(MESSAGE);
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
                    .addDetection(License.of(LICENSE), 100, FILE, 1, 2)));

            final var result = interactor.findScans(FROM, UNTIL);

            assertThat(result).hasSize(1);
            final var pkg = result.get(0);
            assertThat(pkg.purl).isEqualTo(PURL);
        }

        @Test
        void findsErrors() {
            when(store.scanErrors()).thenReturn(List.of(new Scan(PACKAGE, null).setError("Error")));

            final var result = interactor.findErrors();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).error).isNotEmpty();
        }

        @Test
        void findsContestedScans() {
            when(store.contested()).thenReturn(List.of(new Scan(PACKAGE, null).contest()));

            final var result = interactor.findContested();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isContested).isTrue();
        }

        @Test
        void contestsScan() {
            final var scan = new Scan(PACKAGE, null);
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(scan));

            interactor.contest(SCAN_ID);

            assertThat(scan.isContested()).isTrue();
        }

        @Test
        void confirmsLicense() {
            final var scan = new Scan(PACKAGE, null)
                    .confirm(License.of(LICENSE));
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(scan));

            interactor.curateLicense(SCAN_ID, null);

            assertThat(scan.getLicense()).isEqualTo(License.of(LICENSE));
        }

        @Test
        void curatesLicense() {
            final var scan = new Scan(PACKAGE, null);
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(scan));

            interactor.curateLicense(SCAN_ID, LICENSE);

            assertThat(scan.getLicense()).isEqualTo(License.of(LICENSE));
        }

        @Test
        void deletesScansForPackage() {
            interactor.deleteScans(PURL);

            verify(store).deleteScans(PACKAGE);
        }

        @Test
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        void ignoresDetection() {
            final var scan = new Scan(PACKAGE, null)
                    .addDetection(License.of(OTHER), 100, FILE, 1, 2)
                    .addDetection(License.of(LICENSE), 100, FILE, 1, 2);
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(scan));

            interactor.ignore(SCAN_ID, LICENSE);

            assertThat(scan.getDetection(License.of(OTHER)).get().isIgnored()).isFalse();
            assertThat(scan.getDetection(License.of(LICENSE)).get().isIgnored()).isTrue();
        }

        @Test
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        void restoresIgnoredDetection() {
            final var scan = new Scan(PACKAGE, null)
                    .addDetection(License.of(LICENSE), 100, FILE, 1, 2);
            scan.getDetection(License.of(LICENSE)).get().setIgnored(true);
            when(store.getScan(SCAN_ID)).thenReturn(Optional.of(scan));

            interactor.restore(SCAN_ID, LICENSE);

            assertThat(scan.getDetection(License.of(LICENSE)).get().isIgnored()).isFalse();
        }
    }
}
