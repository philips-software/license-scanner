package com.philips.research.licensescanner.core.domain;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ScanErrorTest {

    private static final Instant TIMESTAMP = Instant.now();
    private static final Package PACKAGE = new Package("Namespace", "Name", "version");
    private static final URI LOCATION = URI.create("https://example.com");
    private static final String MESSAGE = "Error message";

    @Test
    void createsInstance() {
        final var error = new ScanError(TIMESTAMP, PACKAGE, LOCATION, MESSAGE);

        assertThat(error.getTimestamp()).isEqualTo(TIMESTAMP);
        assertThat(error.getPackage()).isEqualTo(PACKAGE);
        assertThat(error.getLocation()).isEqualTo(LOCATION);
        assertThat(error.getMessage()).isEqualTo(MESSAGE);
    }
}
