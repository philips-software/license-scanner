package com.philips.research.licensescanner.core.domain.license;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopyrightTest {
    final Copyright copyright = new Copyright();

    @Test
    void createsInstance() {
        assertThat(copyright.getLicenses()).isEmpty();
    }

    @Test
    void tracksUniqueLicenses() {
        final var license = License.of("License");

        copyright.addLicense(license);
        copyright.addLicense(license);

        assertThat(copyright.getLicenses()).containsExactly(license);
    }
}
