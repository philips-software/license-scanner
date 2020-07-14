package com.philips.research.licensescanner.core.domain;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ScanTest {
    private static final Package PACKAGE = new Package("Origin", "Package", "Version");
    private static final String LICENSE = "License";
    private static final URI LOCATION = URI.create("git+ssh://example.come");

    private final Scan scan = new Scan(PACKAGE, LICENSE, LOCATION);

    @Test
    void createsInstance() {
        assertThat(scan.getPackage()).isEqualTo(PACKAGE);
        assertThat(scan.getLicense()).contains(LICENSE);
        assertThat(scan.getVcsUri()).contains(LOCATION);
    }
}
