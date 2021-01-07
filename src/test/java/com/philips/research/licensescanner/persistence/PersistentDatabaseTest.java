/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.license.License;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ComponentScan(basePackageClasses = {PersistentDatabase.class})
@DataJpaTest
class PersistentDatabaseTest {
    private static final URI PURL = URI.create("pkg:package@version");
    private static final URI PURL2 = URI.create("pkg:package2@version");
    private static final URI PURL3 = URI.create("pkg:package3@version");
    private static final URI PURL4 = URI.create("pkg:package4@version");
    private static final URI LOCATION = URI.create("git+http://example.com");
    private static final License LICENSE = License.of("License");
    private static final int SCORE = 42;
    private static final int START_LINE = 12;
    private static final int END_LINE = 15;
    private static final File FILE = new File("test.txt");

    @Autowired
    PersistentDatabase database;

    @Autowired
    ScanRepository scanRepository;

    @Test
    void findScanByPurl() {
        final var scan = database.createScan(PURL, LOCATION);

        assertThat(database.getScan(PURL)).contains(scan);
    }

    @Test
    void findsFilteredScanResults() {
        final var scan1 = database.createScan(URI.create("pkg:namespace/filter@version"), null);
        final var scan2 = database.createScan(URI.create("pkg:namespace/other@version"), null);
        final var scan3 = database.createScan(URI.create("pkg:namespace/filter@other"), null);

        assertThat(database.findScans("duh", "", "")).isEmpty();
        assertThat(database.findScans("", "", "oth")).containsExactly(scan3);
        assertThat(database.findScans("spa", "ilt", "")).containsExactlyInAnyOrder(scan1, scan3);
        assertThat(database.findScans("SPA", "", "")).containsExactlyInAnyOrder(scan1, scan2, scan3);
        assertThat(database.findScans("", "", "")).hasSize(3);
    }

    @Test
    void findsScanErrors() {
        final var good = database.createScan(PURL, LOCATION);
        final var scan = database.createScan(PURL, LOCATION).setError("Error");
        scanRepository.save((ScanEntity) good);
        scanRepository.save((ScanEntity) scan);

        final var errors = database.scanErrors();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getError()).isNotEmpty();
    }

    @Test
    void findsContestedScans() {
        final var normal = database.createScan(PURL, LOCATION);
        final var scan = database.createScan(PURL, LOCATION).contest(LICENSE);
        scanRepository.save((ScanEntity) normal);
        scanRepository.save((ScanEntity) scan);

        final var errors = database.contested();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getContesting()).contains(LICENSE);
    }

    @Test
    void findsLatestScanResultsForPeriod() {
        database.createScan(PURL2, LOCATION);
        database.createScan(PURL, LOCATION);
        final var from = Instant.now();
        database.createScan(PURL2, LOCATION);
        database.createScan(PURL, LOCATION);
        final var until = Instant.now();
        database.createScan(PURL, LOCATION);
        database.createScan(PURL2, LOCATION);

        final var scans = database.findScans(from, until);

        assertThat(scans).hasSize(2);
        assertThat(scans.get(0).getPurl()).isEqualTo(PURL);
    }

    @Test
    void countsNumberOfDetectedLicenses() {
        database.createScan(PURL, LOCATION).setError("Error!");
        database.createScan(PURL2, LOCATION).confirm(LICENSE);
        database.createScan(PURL3, LOCATION).confirm(LICENSE);

        final var count = database.countLicenses();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countsNumberOfScanningErrors() {
        database.createScan(PURL, LOCATION).confirm(LICENSE);
        database.createScan(PURL2, LOCATION).setError("Error!");
        database.createScan(PURL3, LOCATION).setError("Error!");

        final var count = database.countErrors();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countsNumberOfContestedLicenses() {
        database.createScan(PURL, LOCATION).setError("Error");
        database.createScan(PURL2, LOCATION).confirm(LICENSE);
        database.createScan(PURL3, LOCATION).contest(LICENSE);
        database.createScan(PURL4, LOCATION).contest(LICENSE);

        final var count = database.countContested();

        assertThat(count).isEqualTo(2);
    }
}
