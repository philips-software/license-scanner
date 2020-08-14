package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.BusinessException;
import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.download.Downloader;
import com.philips.research.licensescanner.core.domain.license.Copyright;
import com.philips.research.licensescanner.core.domain.license.Detector;
import com.philips.research.licensescanner.core.domain.license.License;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

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
    private static final Scan SCAN = new Scan(PACKAGE, LICENSE, LOCATION);
    private static final Path WORK_DIR = Path.of("not", "for", "real");
    private static final Instant UNTIL = Instant.now();
    private static final Instant FROM = UNTIL.minus(Duration.ofDays(5));

    private final Downloader downloader = mock(Downloader.class);
    private final Detector detector = mock(Detector.class);
    private final PackageStore store = mock(PackageStore.class);

    private final LicenseService service = new LicenseInteractor(store, downloader, detector);

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

            assertThat(info.licenses).contains(LICENSE);
            assertThat(info.location).isEqualTo(LOCATION);
        }
    }

    @Nested
    class ScanPackage {
        Path directory;

        @BeforeEach
        void beforeEach() throws Exception {
            directory = Files.createTempDirectory("test");
        }

        @AfterEach
        void afterEach() throws Exception {
            FileSystemUtils.deleteRecursively(directory);
        }

        @Test
        void downloadsAndScansPackage() throws Exception {
            when(downloader.download(LOCATION)).thenReturn(directory);
            when(detector.scan(directory)).thenReturn(new Copyright(License.of(LICENSE)));

            service.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(detector).scan(directory);
            verify(store).createScan(PACKAGE, LICENSE, LOCATION);
            assertThat(directory.toFile()).doesNotExist();
        }

        @Test
        void registersExceptionAsScanFailure() {
            var message = "Test error";
            when(downloader.download(LOCATION)).thenReturn(directory);
            when(detector.scan(directory)).thenThrow(new BusinessException(message));

            service.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(store).registerScanError(PACKAGE, LOCATION, message);
            verify(store, never()).createScan(PACKAGE, LICENSE, LOCATION);
            assertThat(directory.toFile()).doesNotExist();
        }
    }

    @Nested
    class QueryScanResults {
        @Test
        void findsScansForPeriod() {
            when(store.findScans(FROM, UNTIL)).thenReturn(List.of(new Scan(PACKAGE, LICENSE, LOCATION)));

            final var result = service.findScans(FROM, UNTIL);

            assertThat(result).hasSize(1);
            final var pkg = result.get(0);
            assertThat(pkg.name).isEqualTo(NAME);
            assertThat(pkg.licenses).contains(LICENSE);
            assertThat(pkg.location).isEqualTo(LOCATION);
        }
    }
}
