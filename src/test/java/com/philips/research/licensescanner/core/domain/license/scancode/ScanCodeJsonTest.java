/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain.license.scancode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCodeJsonTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Package PACKAGE = new Package(URI.create("pkg:package@version"));
    private static final URI LOCATION = URI.create("http://example.com");
    private static final Path DIRECTORY = Path.of("src", "test", "resources", "scancode");

    private Scan processFile(String filename) throws java.io.IOException {
        final var scanResult = MAPPER.readValue(DIRECTORY.resolve(filename).toFile(), ScanCodeJson.class);
        final var scan = new Scan(PACKAGE, LOCATION);

        scanResult.addScanResultsTo(scan);
        return scan;
    }

    @Test
    void populatesScan() throws Exception {
        final Scan scan = processFile("simple.json");

        assertThat(scan.getLicense()).isEqualTo(License.of("key"));
        final var detection = scan.getDetections().get(0);
        assertThat(detection.getConfirmations()).isEqualTo(1);
        assertThat(detection.getLicense()).isEqualTo(License.of("key"));
        assertThat(detection.getFilePath().getPath()).isEqualTo("file.txt");
        assertThat(detection.getScore()).isEqualTo(42);
        assertThat(detection.getStartLine()).isEqualTo(14);
        assertThat(detection.getEndLine()).isEqualTo(21);
    }

    @Test
    void buildsComplexLicenseExpressions() throws Exception {
        final Scan scan = processFile("expressions.json");

        assertThat(scan.getLicense()).isEqualTo(LicenseParser.parse("a AND (b OR c)"));
        assertThat(scan.getDetections()).hasSize(2);
        final var detection1 = scan.getDetections().get(0);
        assertThat(detection1.getLicense()).isEqualTo(License.of("a"));
        assertThat(detection1.getFilePath().getPath()).isEqualTo("file.txt");
        assertThat(detection1.getScore()).isEqualTo(42);
        assertThat(detection1.getStartLine()).isEqualTo(4);
        assertThat(detection1.getEndLine()).isEqualTo(8);
        final var detection2 = scan.getDetections().get(1);
        assertThat(detection2.getLicense()).isEqualTo(LicenseParser.parse("b OR c"));
        assertThat(detection2.getFilePath().getPath()).isEqualTo("file.txt");
        assertThat(detection2.getScore()).isEqualTo(23);
        assertThat(detection2.getStartLine()).isEqualTo(6);
        assertThat(detection2.getEndLine()).isEqualTo(14);
    }

    @Test
    void usesSpdxLicenseIdentifiers() throws Exception {
        final Scan scan = processFile("spdx.json");

        assertThat(scan.getLicense()).isEqualTo(LicenseParser.parse("MIT WITH Exception OR (AFL AND xyzzy)"));
        final var detection = scan.getDetections().get(0);
        assertThat(detection.getLicense()).isEqualTo(scan.getLicense());
    }
}
