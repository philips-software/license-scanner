package com.philips.research.licensescanner.core.domain.license;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopyrightTest {

    private static final License LICENSE = License.of("License");

    @Test
    void createsInstance() {
        final var copyright = new Copyright(LICENSE);

        assertThat(copyright.getLicense()).isEqualTo(LICENSE);
    }
}
