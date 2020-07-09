package com.philips.research.licensescanner.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ComponentScan(basePackageClasses = {PackageDatabase.class})
@DataJpaTest
class PackageDatabaseTest {
    private static final String ORIGIN = "Origin";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";

    @Autowired
    PackageDatabase database;

    @Autowired
    PackageRepository repo;

    @Test
    void createsPackages() {
        database.createPackage(ORIGIN, NAME, "1");
        database.createPackage(ORIGIN, NAME, "2");
        final var expected = database.createPackage(ORIGIN, NAME, VERSION);
        database.createPackage(ORIGIN, NAME, "1");
        database.createPackage(ORIGIN, NAME, "2");

        for (PackageEntity packageEntity : repo.findAll()) {
            System.out.println("Found: " + packageEntity);
        }

        final var pkg = database.findPackage(ORIGIN, NAME, VERSION).get();

        assertThat(expected).isEqualTo(pkg);
    }
}
