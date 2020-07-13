package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.download.VcsUri;
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
    private static final VcsUri VCS_URI = VcsUri.from(URI.create("git+http://example.com"));

    @Autowired
    PackageDatabase database;

    @Autowired
    PackageRepository packageRepository;

    @Autowired
    ScanRepository scanRepository;

    @Test
    void createsPackages() {
        database.createPackage(ORIGIN, NAME, "1");
        database.createPackage(ORIGIN, NAME, "2");
        final var expected = database.createPackage(ORIGIN, NAME, VERSION);
        database.createPackage(ORIGIN, NAME, "1");
        database.createPackage(ORIGIN, NAME, "2");

        for (PackageEntity packageEntity : packageRepository.findAll()) {
            System.out.println("Found: " + packageEntity);
        }

        final var pkg = database.findPackage(ORIGIN, NAME, VERSION).get();

        assertThat(expected).isEqualTo(pkg);
    }

    @Test
    void createsScans() {
        final var pkg = database.createPackage(ORIGIN, NAME, VERSION);
        final var expected = (ScanEntity) database.createScan(pkg, LICENSE, VCS_URI);

        final var scan = scanRepository.findById(expected.id);
        assertThat(scan).isNotNull();

        packageRepository.save((PackageEntity) pkg);
        scanRepository.save(expected);

//        final var latest = database.latestScan(pkg).get();
//
//        assertThat(latest.getLicense()).isEqualTo(expected.getLicense());
    }
}
