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

public class DetectorException extends RuntimeException {
    public DetectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
