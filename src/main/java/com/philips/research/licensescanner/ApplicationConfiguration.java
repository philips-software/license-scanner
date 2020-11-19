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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;

/**
 * Spring custom application configuration.
 * This is used by Spring to populate a configuration object from its many configuration inputs.
 */
@Validated
@ConfigurationProperties(prefix = "licenses")
public class ApplicationConfiguration {
    @NotNull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Path tempDir;
    private int thresholdPercent = 50;
    private int cacheSize = 10;

    /**
     * @return The common working directory.
     */
    public Path getTempDir() {
        return tempDir;
    }

    /**
     * @param directory An existing directory on this system.
     */
    public ApplicationConfiguration setTempDir(Path directory) {
        if (!directory.toFile().isDirectory()) {
            throw new ValidationException(directory + " is not a valid working directory");
        }
        this.tempDir = directory;
        return this;
    }

    /**
     * @return size of the source download cache
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Configures the number of cached packages
     */
    public ApplicationConfiguration setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    /**
     * @return license probability threshold as a percentage.
     */
    public int getThresholdPercent() {
        return thresholdPercent;
    }

    /**
     * Sets license probability threshold.
     *
     * @param thresholdPercent percentage certainty about the detected license
     */
    public ApplicationConfiguration setThresholdPercent(int thresholdPercent) {
        this.thresholdPercent = Math.min(Math.max(0, thresholdPercent), 100);
        return this;
    }
}
