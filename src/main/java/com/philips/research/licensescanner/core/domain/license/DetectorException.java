package com.philips.research.licensescanner.core.domain.license;

/**
 * Exception thrown for problems encountered while scanning copyright information from package sources.
 */
public class DetectorException extends RuntimeException {
    public DetectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
