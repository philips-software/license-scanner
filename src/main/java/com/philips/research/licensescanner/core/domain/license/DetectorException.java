/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.license;

public class DetectorException extends RuntimeException {
    public DetectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
