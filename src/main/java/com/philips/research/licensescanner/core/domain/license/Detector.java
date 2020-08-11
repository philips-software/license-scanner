package com.philips.research.licensescanner.core.domain.license;

import java.nio.file.Path;

/**
 * License scanner interface API.
 */
public interface Detector {
    /**
     * Scans copyright information from a directory of package files.
     *
     * @param directory location of the package files
     * @return scan results
     */
    Copyright scan(Path directory);
}
