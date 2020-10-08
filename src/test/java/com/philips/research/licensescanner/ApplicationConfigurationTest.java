/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationConfigurationTest {
    @Autowired
    ApplicationConfiguration config;

    @Test
    void configurationIsInitialized() {
        assertThat(config.getTempDir().toFile().isDirectory()).isTrue();
        assertThat(config.getThresholdPercent()).isNotZero();
    }
}

