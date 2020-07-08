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
    }
}

