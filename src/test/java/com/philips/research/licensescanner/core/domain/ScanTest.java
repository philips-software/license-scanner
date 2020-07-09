package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.download.VcsUri;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ScanTest {
    private static final Package PACKAGE = new Package("Origin", "Package", "Version");
    private static final String LICENSE = "License";
    private static final VcsUri VCS_URI = VcsUri.from(URI.create("git+ssh://example.come"));

    private final Scan scan = new Scan(PACKAGE);

    @Test
    void createsInstance() {
        assertThat(scan.getPackage()).isEqualTo(PACKAGE);
        assertThat(scan.getLicense()).isEmpty();
        assertThat(scan.getVcsUri()).isEmpty();
    }

    @Test
    void updatesLicense() {
        scan.setLicense(LICENSE);

        assertThat(scan.getLicense()).contains(LICENSE);
    }

    @Test
    void updatesVcsUri() {
        scan.setVcsUri(VCS_URI);

        assertThat(scan.getVcsUri()).contains(VCS_URI);
    }
}
