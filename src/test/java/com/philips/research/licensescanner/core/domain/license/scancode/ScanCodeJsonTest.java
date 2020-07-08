package com.philips.research.licensescanner.core.domain.license.scancode;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCodeJsonTest {
    private static final LicenseJson LICENSE_1 = new LicenseJson("bsd2", "BSD-2-Clause");
    private static final LicenseJson LICENSE_2 = new LicenseJson("mit", "MIT");

    @Test
    void listsSpdxLicensesPerFile() {
        final var scan = new ScanCodeJson(List.of(
                new FileJson(List.of(LICENSE_1, LICENSE_2), LICENSE_1.key, LICENSE_2.key)
        ));

        assertThat(scan.getLicense()).isEqualTo(LICENSE_1.spdx + " AND " + LICENSE_2.spdx);
    }

    @Test
    void filtersDuplicateLicenses() {
        final var scan = new ScanCodeJson(List.of(
                new FileJson(List.of(LICENSE_1, LICENSE_1), LICENSE_1.key)
        ));

        assertThat(scan.getLicense()).isEqualTo(LICENSE_1.spdx);
    }

    @Test
    void defaultsUnknownSpdxIdentifiers() {
        final var license = new LicenseJson("key", "");

        assertThat(license.getSpdxIdentifier()).isEqualTo("Unknown");
    }

    @Test
    void supportsLicenseCombinations() {
        final var scan = new ScanCodeJson(List.of(
                new FileJson(List.of(LICENSE_1, LICENSE_2), LICENSE_1.key + " AND " + LICENSE_2.key)
        ));

        assertThat(scan.getLicense()).isEqualTo("(" + LICENSE_1.spdx + " AND " + LICENSE_2.spdx + ")");
    }

    @Test
    void groupsLicenseCombinations() {
        final var scan = new ScanCodeJson(List.of(
                new FileJson(List.of(LICENSE_1, LICENSE_2), LICENSE_1.key, LICENSE_1.key + " OR " + LICENSE_2.key)
        ));

        assertThat(scan.getLicense()).isEqualTo("(" + LICENSE_1.spdx + " OR " + LICENSE_2.spdx + ") AND " + LICENSE_1.spdx);
    }
}
