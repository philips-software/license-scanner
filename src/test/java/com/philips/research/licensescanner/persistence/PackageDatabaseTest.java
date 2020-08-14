package com.philips.research.licensescanner.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ComponentScan(basePackageClasses = {PackageDatabase.class})
@DataJpaTest
class PackageDatabaseTest {
    private static final String ORIGIN = "Origin";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";
    private static final URI LOCATION = URI.create("git+http://example.com");
    private static final String MESSAGE = "Message";

    @Autowired
    PackageDatabase database;

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    ScanRepository scanRepository;

    @Test
    void getsPackage() {
        database.createPackage(ORIGIN, NAME, "other");
        final var expected = database.createPackage(ORIGIN, NAME, VERSION);

        final var pkg = database.getPackage(ORIGIN, NAME, VERSION);

        assertThat(pkg).contains(expected);
    }

    @Test
    void empty_packageDoesNotExist() {
        database.createPackage(ORIGIN, NAME, "other");

        assertThat(database.getPackage(ORIGIN, NAME, VERSION)).isEmpty();
    }

    @Test
    void findsMatchingPackages() {
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        database.createPackage(ORIGIN, "Other", VERSION);
        database.createPackage(ORIGIN, NAME, "Other");

        assertThat(database.findPackages(ORIGIN, NAME, VERSION)).containsExactly(pkg);
        assertThat(database.findPackages("", "", "")).hasSize(3);
        assertThat(database.findPackages(ORIGIN, NAME, "")).hasSize(2);
        assertThat(database.findPackages("Other", NAME, VERSION)).isEmpty();
    }

    @Test
    void findsLatestValidScanResult() throws Exception {
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        database.createScan(pkg, "other", LOCATION);
        database.createScan(pkg, LICENSE, LOCATION);

        //noinspection OptionalGetWithoutIsPresent
        final var latest = database.latestScan(pkg).get();

        assertThat(latest.getPackage()).isEqualTo(pkg);
        assertThat(latest.getLicense()).contains(LICENSE);
    }

    @Test
    void empty_noLatestScanResult() {
        final var decoy = database.createPackage(ORIGIN, "Decoy", VERSION);
        database.createScan(decoy, LICENSE, LOCATION);
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        database.createScan(pkg, null, LOCATION);

        assertThat(database.latestScan(pkg)).isEmpty();
    }

    @Test
    void registersScanningError() {
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        database.registerScanError(pkg, LOCATION, "first");
        database.registerScanError(pkg, LOCATION, MESSAGE);
        final var decoy = database.createPackage(ORIGIN, "Decoy", VERSION);
        database.createScan(decoy, LICENSE, LOCATION);
        database.registerScanError(decoy, LOCATION, MESSAGE);

        final var errors = database.scanErrors(pkg);

        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).getPackage()).isEqualTo(pkg);
        assertThat(errors.get(0).getMessage()).isEqualTo(MESSAGE);
        assertThat(database.latestScan(pkg)).isEmpty();
    }
}
