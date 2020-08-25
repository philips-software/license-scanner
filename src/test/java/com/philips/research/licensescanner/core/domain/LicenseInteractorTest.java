package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.BusinessException;
import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Detector;
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
    private static final Instant UNTIL = Instant.now();
    private static final Instant FROM = UNTIL.minus(Duration.ofDays(5));
    private static final int THRESHOLD = 70;

    private final Downloader downloader = mock(Downloader.class);
    private final Detector detector = mock(Detector.class);
    private final PackageStore store = mock(PackageStore.class);

    private final LicenseService service = new LicenseInteractor(store, downloader, detector, THRESHOLD);

    @BeforeEach
    void beforeEach() {
        when(store.getPackage(ORIGIN, NAME, VERSION)).thenReturn(Optional.of(PACKAGE));
    }

    @Nested
    class FindPackages {
        @Test
        void findsPackages() {
            when(store.findPackages(ORIGIN, NAME, VERSION)).thenReturn(List.of(new Package(ORIGIN, NAME, VERSION)));

            final var result = service.findPackages(ORIGIN, NAME, VERSION);

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

            final var noInfo = service.licenseFor(ORIGIN, NAME, VERSION);

            assertThat(noInfo).isEmpty();
        }

        @Test
        void noLicense_noScanForPackage() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.empty());

            final var noInfo = service.licenseFor(ORIGIN, NAME, VERSION);

            assertThat(noInfo).isEmpty();
        }

        @Test
        void retrievesLicenseForPackage() {
            when(store.latestScan(PACKAGE)).thenReturn(Optional.of(SCAN));

            @SuppressWarnings("OptionalGetWithoutIsPresent") final var info = service.licenseFor(ORIGIN, NAME, VERSION).get();

            assertThat(info.license).contains(LICENSE);
            assertThat(info.location).isEqualTo(LOCATION);
        }
    }

    @Nested
    class ScanPackage {
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

            service.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(store, never()).createScan(PACKAGE, LOCATION);
            verify(detector, never()).scan(any(Path.class), any(Scan.class), anyInt());
        }

        @Test
        void downloadsAndScansPackage() {
            when(downloader.download(LOCATION)).thenReturn(directory);

            service.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(detector).scan(directory, scan, THRESHOLD);
            assertThat(directory.toFile()).doesNotExist();
        }

        @Test
        void registersExceptionAsScanFailure() {
            final var message = "Test error";
            when(downloader.download(LOCATION)).thenReturn(directory);
            doThrow(new BusinessException(message)).when(detector).scan(directory, scan, THRESHOLD);

            service.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            assertThat(scan.getError()).contains(message);
            assertThat(directory.toFile()).doesNotExist();
        }
    }

    @Nested
    class QueryScanResults {
        @Test
        void findsScansForPeriod() {
            when(store.findScans(FROM, UNTIL)).thenReturn(List.of(new Scan(PACKAGE, LOCATION)
                    .addDetection(License.of(LICENSE), 100, null, 1, 2)));

            final var result = service.findScans(FROM, UNTIL);

            assertThat(result).hasSize(1);
            final var pkg = result.get(0);
            assertThat(pkg.pkg.name).isEqualTo(NAME);
            assertThat(pkg.license).contains(LICENSE);
            assertThat(pkg.location).isEqualTo(LOCATION);
        }
    }
}
