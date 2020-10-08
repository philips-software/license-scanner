/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain.license;

import com.philips.research.licensescanner.core.domain.Scan;

import java.nio.file.Path;

/**
 * License scanner interface API.
 */
public interface Detector {
    /**
     * Scans copyright information from a directory of package files.
     *
     * @param directory      location of the package files
     * @param scan           scan result
     * @param scoreThreshold minimal percentage detection certainty
     */
    void scan(Path directory, Scan scan, int scoreThreshold);
}
