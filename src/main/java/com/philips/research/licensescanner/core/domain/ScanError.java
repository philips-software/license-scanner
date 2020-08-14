package com.philips.research.licensescanner.core.domain;

import java.net.URI;
import java.time.Instant;

public class ScanError {
    private final Instant timestamp ;
    private final Package pkg;
    private final URI location;
    private final String message;

    public ScanError(Instant timestamp, Package pkg, URI location, String message) {
        this.timestamp = timestamp;
        this.pkg = pkg;
        this.location = location;
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Package getPackage() {
        return pkg;
    }

    public URI getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }
}
