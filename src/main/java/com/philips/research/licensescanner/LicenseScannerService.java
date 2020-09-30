/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application entry point.
 */
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(ApplicationConfiguration.class)
public class LicenseScannerService {
    public static void main(String[] args) {
        SpringApplication.run(LicenseScannerService.class, args);
    }
}
