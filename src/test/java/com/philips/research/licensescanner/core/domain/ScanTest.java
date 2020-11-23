/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ScanTest {
    private static final Package PACKAGE = new Package(URI.create("pkg:package@version"));
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
        Instant now = Instant.now();
        assertThat(scan.getUuid()).isNotNull();
        assertThat(scan.getTimestamp()).isBetween(now.minus(Duration.ofSeconds(1)), now);
        assertThat(scan.getPackage()).isEqualTo(PACKAGE);
        assertThat(scan.getLicense()).isEqualTo(License.NONE);
        assertThat(scan.getLocation()).contains(LOCATION);
        assertThat(scan.getContesting()).isEmpty();
        assertThat(scan.isOverride()).isFalse();
        assertThat(scan.getError()).isEmpty();
        assertThat(scan.getDetections()).isEmpty();
        assertThat(scan.getDetection(LICENSE)).isEmpty();
    }

    @Test
    void contestsScan() {
        scan.contest(LICENSE);

        assertThat(scan.getContesting()).contains(LICENSE);
    }

    @Test
    void ignoresContestWithLessDetail() {
        scan.contest(LICENSE);
        scan.contest(License.NONE);
    }

    @Test
    void noContest_licenseWasAlreadyConfirmed() {
        scan.confirm(LICENSE);

        scan.contest(License.of("Contest"));

        assertThat(scan.getContesting()).isEmpty();
    }

    @Test
    void confirmsContestedScan() {
        final var license = License.of("Confirmed");
        scan.contest(LICENSE);

        scan.confirm(license);

        assertThat(scan.isOverride()).isTrue();
        assertThat(scan.getLicense()).isEqualTo(license);
        assertThat(scan.getContesting()).isEmpty();
    }

    @Test
    void clearsErrorOnConfirm() {
        scan.setError("Something went wrong");

        scan.confirm(LICENSE);

        assertThat(scan.getError()).isEmpty();
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
        //noinspection OptionalGetWithoutIsPresent
        final var detection = scan.getDetection(LICENSE).get();
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
        final var detection = scan.getDetection(LICENSE).get();
        assertThat(detection.getConfirmations()).isEqualTo(2);
    }

    @Test
    void setsLicenseFromDetections() {
        final var skip = License.of("Skip me");
        scan.addDetection(License.of("A"), SCORE, FILE, START_LINE, END_LINE);
        scan.addDetection(skip, SCORE, FILE, START_LINE, END_LINE);
        scan.getDetection(skip).ifPresent(detection -> detection.setIgnored(true));
        scan.addDetection(License.of("B"), SCORE, FILE, START_LINE, END_LINE);

        assertThat(scan.getLicense()).isEqualTo(LicenseParser.parse("A and B"));
    }

    @Test
    void clearsContesting_detectedMatches() {
        scan.contest(LICENSE);

        scan.addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);

        assertThat(scan.getContesting()).isEmpty();
    }

    @Test
    void clearsContesting_updatedDetectionMatches() {
        final var other = License.of("Other");
        scan.addDetection(other, SCORE, FILE, START_LINE, END_LINE);
        scan.addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);
        scan.contest(LICENSE);

        //noinspection OptionalGetWithoutIsPresent
        scan.ignore(other, true);

        assertThat(scan.getContesting()).isEmpty();
    }
}
