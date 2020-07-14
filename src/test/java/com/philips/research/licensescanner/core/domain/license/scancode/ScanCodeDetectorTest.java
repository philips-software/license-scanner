package com.philips.research.licensescanner.core.domain.license.scancode;

import com.philips.research.licensescanner.core.domain.download.AnonymousHandler;
import com.philips.research.licensescanner.core.domain.license.Detector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCodeDetectorTest {
    private final Detector detector = new ScanCodeDetector();

    private Path tempDir;

    @BeforeEach
    public void beforeEach() throws Exception {
        tempDir = Files.createTempDirectory("test");
        var location = Path.of("src", "test", "resources", "sample.zip").toUri();
        new AnonymousHandler().download(tempDir, location);
    }

    @AfterEach
    public void afterEach() throws Exception {
        FileSystemUtils.deleteRecursively(tempDir);
    }

    @Test
    void decompressesAndScansDirectory() {
        final var result = detector.scan(tempDir);

        assertThat(result.license).isNotEmpty();
        //TODO Just debugging ...
        System.out.println("License: " + result.license);
    }
}
