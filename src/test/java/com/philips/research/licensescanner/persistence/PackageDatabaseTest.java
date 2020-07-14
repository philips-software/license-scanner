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

    @Autowired
    PackageDatabase database;

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    ScanRepository scanRepository;

    @Test
    void findsPackage() {
        database.createPackage(ORIGIN, NAME, "other");
        final var expected = database.createPackage(ORIGIN, NAME, VERSION);

        final var pkg = database.findPackage(ORIGIN, NAME, VERSION);

        assertThat(pkg).contains(expected);
    }

    @Test
    void empty_packageDoesNotExist() {
        assertThat(database.findPackage(ORIGIN, NAME, VERSION)).isEmpty();
    }

    @Test
    void findsLatestValidScanResult() throws Exception {
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        database.createScan(pkg, "other", LOCATION);
        database.createScan(pkg, LICENSE, LOCATION);

        //noinspection OptionalGetWithoutIsPresent
        final var latest = database.latestScan(pkg).get();

        assertThat(latest.getLicense()).contains(LICENSE);
    }

    @Test
    void empty_noLatestScanResult() {
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        database.createScan(pkg, null, LOCATION);

        assertThat(database.latestScan(pkg)).isEmpty();
    }
}
