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
     * @param scoreThreshold minimal percentage detection certainty
     * @return scan results
     */
    Copyright scan(Path directory, int scoreThreshold);
}
