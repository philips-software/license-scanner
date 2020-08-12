package com.philips.research.licensescanner.core.domain;

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

    private final Downloader downloader = mock(Downloader.class);
    private final Detector detector = mock(Detector.class);
    private final PackageStore store = mock(PackageStore.class);

    private final LicenseService service = new LicenseInteractor(store, downloader, detector);

    @BeforeEach
    void beforeEach() {
        when(store.findPackage(ORIGIN, NAME, VERSION)).thenReturn(Optional.of(PACKAGE));
    }

    @Nested
    class QueryLicenseInformation {
        @Test
        void noLicense_packageNotFound() {
            when(store.findPackage(ORIGIN, NAME, VERSION)).thenReturn(Optional.empty());

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
            when(detector.scan(directory)).thenReturn(new Copyright().addLicense(License.of(LICENSE)));

            service.scanLicense(ORIGIN, NAME, VERSION, LOCATION);

            verify(detector).scan(directory);
            verify(store).createScan(PACKAGE, LICENSE, LOCATION);
            assertThat(directory.toFile()).doesNotExist();
        }
    }
}
