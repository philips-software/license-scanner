package com.philips.research.licensescanner.core.domain.license;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCodeDetectorTest {
    private final ScanCodeDetector detector = new ScanCodeDetector();

    @Test
    void scansDirectory() {
        final var result = detector.scan(Path.of("src", "test", "resources", "sample"));

        assertThat(result.license).isNotEmpty();
        System.out.println("License: " + result.license);
    }
}
