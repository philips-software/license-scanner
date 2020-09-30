/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.license;

import com.philips.research.licensescanner.core.BusinessException;

public class LicenseException extends BusinessException {
    public LicenseException(String message) {
        super(message);
    }
}
