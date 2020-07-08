package com.philips.research.licensescanner.core.domain.license.scancode;

import com.philips.research.licensescanner.core.domain.license.Detector;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCodeDetectorTest {
    private final Detector detector = new ScanCodeDetector();

    @Test
    void scansDirectory() {
        final var result = detector.scan(Path.of("src", "test", "resources", "sample"));

        assertThat(result.license).isNotEmpty();
        System.out.println("License: " + result.license);
    }
}
