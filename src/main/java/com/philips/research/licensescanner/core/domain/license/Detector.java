/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
