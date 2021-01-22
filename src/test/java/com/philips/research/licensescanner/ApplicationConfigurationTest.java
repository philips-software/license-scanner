/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
        assertThat(config.getCacheSize()).isNotZero();
    }
}

