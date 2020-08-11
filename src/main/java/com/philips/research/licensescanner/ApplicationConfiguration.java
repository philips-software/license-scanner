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
    private Path tempDir;

    /**
     * @return The common working directory.
     */
    public Path getTempDir() {
        return tempDir;
    }

    /**
     * @param directory An existing directory on this system.
     */
    public void setTempDir(Path directory) {
        if (!directory.toFile().isDirectory()) {
            throw new ValidationException(directory + " is not a valid working directory");
        }
        this.tempDir = directory;
    }
}
