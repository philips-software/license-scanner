package com.philips.research.licensescanner.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class CoreConfiguration {
    @Bean(name = "licenseDetectionExecutor")
    public Executor threadPoolTaskExecutor() {
        final var executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(3);
        return executor;
    }
}
