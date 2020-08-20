package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.license.License;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ScanTest {
    private static final Package PACKAGE = new Package("Origin", "Package", "Version");
    private static final License LICENSE = License.of("License");
    private static final URI LOCATION = URI.create("git+ssh://example.come");
    private static final int START_LINE = 10;
    private static final int END_LINE = 20;
    private static final int SCORE = 42;
    private static final File FILE = new File("path/to/file");
    private static final String MESSAGE = "Message";

    private final Scan scan = new Scan(PACKAGE, LOCATION);

    @Test
    void createsInstance() {
        assertThat(scan.getPackage()).isEqualTo(PACKAGE);
        assertThat(scan.getLicense()).isEqualTo(License.NONE);
        assertThat(scan.getLocation()).contains(LOCATION);
        assertThat(scan.getError()).isEmpty();
        assertThat(scan.getDetections()).isEmpty();
    }

    @Test
    void raisesError() {
        scan.setError(MESSAGE);

        assertThat(scan.getError()).contains(MESSAGE);
    }

    @Test
    void addsNewDetection() {
        scan.addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);

        assertThat(scan.getLicense()).isEqualTo(LICENSE);
        assertThat(scan.getDetections()).hasSize(1);
        final var detection = scan.getDetections().get(0);
        assertThat(detection.getScore()).isEqualTo(SCORE);
        assertThat(detection.getFilePath()).isEqualTo(FILE);
        assertThat(detection.getStartLine()).isEqualTo(START_LINE);
        assertThat(detection.getEndLine()).isEqualTo(END_LINE);
    }

    @Test
    void addsEvidenceToExistingDetection() {
        scan.addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);
        scan.addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);

        assertThat(scan.getDetections()).hasSize(1);
        final var detection = scan.getDetections().get(0);
        assertThat(detection.getConfirmations()).isEqualTo(2);
    }
}
