package com.philips.research.licensescanner.core.domain.download;

/**
 * Exception thrown in case downloading of package sources failed.
 */
public class DownloadException extends RuntimeException {
    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
