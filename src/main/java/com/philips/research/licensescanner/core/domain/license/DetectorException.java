/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.license;

import com.philips.research.licensescanner.core.BusinessException;

public class DetectorException extends BusinessException {
    public DetectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
