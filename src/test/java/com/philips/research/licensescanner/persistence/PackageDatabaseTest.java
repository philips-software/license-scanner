/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
    private static final String ORIGIN = "Origin";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
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
        pkg = database.createPackage(ORIGIN, NAME, VERSION);
    }

    @Test
    void getsPackage() {
        database.createPackage(ORIGIN, NAME, "other");

        final var result = database.getPackage(ORIGIN, NAME, VERSION);

        assertThat(result).contains(pkg);
    }

    @Test
    void findsFilteredPackages() {
        database.createPackage(ORIGIN, "Other", VERSION);
        database.createPackage(ORIGIN, NAME, "Other");

        assertThat(database.findPackages(ORIGIN, NAME, VERSION)).containsExactly(pkg);
        assertThat(database.findPackages("", "", "")).hasSize(3);
        assertThat(database.findPackages(ORIGIN, NAME, "")).hasSize(2);
        assertThat(database.findPackages("Other", NAME, VERSION)).isEmpty();
    }

    @Test
    void findsLatestNonErrorScanResultForPackage() {
        final var scan = database.createScan(pkg, LOCATION).addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);
        scanRepository.save((ScanEntity) scan);
        final var error = database.createScan(pkg, null).setError("Testing");
        scanRepository.save((ScanEntity) error);

        //noinspection OptionalGetWithoutIsPresent
        final var latest = database.latestScan(pkg).get();

        assertThat(latest.getPackage()).isEqualTo(pkg);
        assertThat(latest.getLocation()).contains(LOCATION);
        assertThat(latest.getLicense()).isEqualTo(LICENSE);
    }

    @Test
    void findsLatestScanError() {
        final var scan = database.createScan(pkg, null).addDetection(LICENSE, SCORE, FILE, START_LINE, END_LINE);
        scan.setError("Boeh!");
        scanRepository.save((ScanEntity) scan);

        final var errors = database.scanErrors(pkg);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getError()).isNotNull();
    }

    @Test
    void findsNoLatestScanResultForPackage() {
        final var decoy = database.createPackage(ORIGIN, "Decoy", VERSION);
        database.createScan(decoy, LOCATION);
        final var error = database.createScan(pkg, LOCATION).setError("Testing");
        scanRepository.save((ScanEntity) error);

        assertThat(database.latestScan(pkg)).isEmpty();
    }

    @Test
    void findsLatestScanResultsForPeriod() {
        final var otherPkg = database.createPackage(ORIGIN, "Other", VERSION);
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
}
