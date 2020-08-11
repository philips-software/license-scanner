package com.philips.research.licensescanner.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class CoreConfiguration {
    private static final int SCAN_PROCESSES = 3;

    /**
     * @return task scheduler implementation for scanning copyright information for packages
     */
    @Bean(name = "licenseDetectionExecutor")
    public Executor threadPoolTaskExecutor() {
        final var executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(SCAN_PROCESSES);
        executor.setCorePoolSize(SCAN_PROCESSES);
        return executor;
    }
}
