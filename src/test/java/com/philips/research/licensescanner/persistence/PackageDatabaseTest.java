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

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.license.License;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ComponentScan(basePackageClasses = {PackageDatabase.class})
@DataJpaTest
class PackageDatabaseTest {
    private static final URI PURL = URI.create("pkg:package@version");
    private static final URI LOCATION = URI.create("git+http://example.com");
    private static final License LICENSE = License.of("License");
    private static final int SCORE = 42;
    private static final int START_LINE = 12;
    private static final int END_LINE = 15;
    private static final File FILE = new File("test.txt");

    @Autowired
    PackageDatabase database;

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    ScanRepository scanRepository;

    Package pkg;

    @BeforeEach
    void beforeEach() {
        pkg = database.createPackage(PURL);
    }

    @Test
    void getsPackage() {
        database.createPackage(URI.create("pkg:other@version"));

        final var result = database.getPackage(PURL);

        assertThat(result).contains(pkg);
    }

    @Test
    void findsFilteredPackages() {
        final var pkg1 = database.createPackage(URI.create("pkg:namespace/filter@version"));
        final var pkg2 = database.createPackage(URI.create("pkg:namespace/other@version"));
        final var pkg3 = database.createPackage(URI.create("pkg:namespace/filter@other"));

        assertThat(database.findPackages("duh", "", "")).isEmpty();
        assertThat(database.findPackages("", "", "oth")).containsExactly(pkg3);
        assertThat(database.findPackages("spa", "ilt", "")).containsExactlyInAnyOrder(pkg1, pkg3);
        assertThat(database.findPackages("spa", "", "")).containsExactlyInAnyOrder(pkg1, pkg2, pkg3);
        assertThat(database.findPackages("", "", "")).hasSize(4);
        assertThat(database.findPackages("", "ack", "ers")).containsExactly(pkg);
    }

    @Test
    void findsLatestScanError() {
        final var scan = database.createScan(pkg, LOCATION).addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);
        scan.setError("Boeh!");
        scanRepository.save((ScanEntity) scan);

        final var errors = database.scanErrors(pkg);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getError()).isNotEmpty();
    }

    @Test
    void findsScanErrors() {
        final var good = database.createScan(pkg, LOCATION);
        final var scan = database.createScan(pkg, LOCATION).setError("Error");
        scanRepository.save((ScanEntity) good);
        scanRepository.save((ScanEntity) scan);

        final var errors = database.scanErrors();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getError()).isNotEmpty();
    }

    @Test
    void findsContestedScans() {
        final var normal = database.createScan(pkg, LOCATION);
        final var scan = database.createScan(pkg, LOCATION).contest(LICENSE);
        scanRepository.save((ScanEntity) normal);
        scanRepository.save((ScanEntity) scan);

        final var errors = database.contested();

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getContesting()).contains(LICENSE);
    }

    @Test
    void findsLatestScanResultsForPeriod() {
        final var otherPkg = database.createPackage(URI.create("pkg:other@version"));
        database.createScan(otherPkg, LOCATION);
        database.createScan(pkg, LOCATION);
        final var from = Instant.now();
        database.createScan(otherPkg, LOCATION);
        database.createScan(pkg, LOCATION);
        final var until = Instant.now();
        database.createScan(pkg, LOCATION);
        database.createScan(otherPkg, LOCATION);

        final var scans = database.findScans(from, until);

        assertThat(scans).hasSize(2);
        assertThat(scans.get(0).getPackage()).isEqualTo(pkg);
    }

    @Test
    void deletesScansForPackage() {
        database.createScan(pkg, LOCATION);

        database.deleteScans(pkg);

        final var now = Instant.now();
        assertThat(database.findScans(now.minus(Duration.ofSeconds(1)), now)).isEmpty();
    }

    @Test
    void findsScanResultById() {
        Instant before = Instant.now();
        database.createScan(pkg, LOCATION);
        database.createScan(pkg, LOCATION);
        Instant after = Instant.now();
        final var scans = database.findScans(before, after);
        assertThat(scans).hasSize(2);
        final var uuid = scans.get(0).getUuid();

        //noinspection OptionalGetWithoutIsPresent
        final var found = database.getScan(uuid).get();

        assertThat(found.getUuid()).isEqualTo(uuid);
    }

    @Test
    void countsNumberOfDetectedLicenses() {
        database.createScan(pkg, LOCATION).setError("Error!");
        database.createScan(pkg, LOCATION).confirm(LICENSE);
        database.createScan(pkg, LOCATION).confirm(LICENSE);

        final var count = database.countLicenses();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countsNumberOfScanningErrors() {
        database.createScan(pkg, LOCATION).confirm(LICENSE);
        database.createScan(pkg, LOCATION).setError("Error!");
        database.createScan(pkg, LOCATION).setError("Error!");

        final var count = database.countErrors();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countsNumberOfContestedLicenses() {
        database.createScan(pkg, LOCATION).setError("Error");
        database.createScan(pkg, LOCATION).confirm(LICENSE);
        database.createScan(pkg, LOCATION).contest(LICENSE);
        database.createScan(pkg, LOCATION).contest(LICENSE);

        final var count = database.countContested();

        assertThat(count).isEqualTo(2);
    }
}
