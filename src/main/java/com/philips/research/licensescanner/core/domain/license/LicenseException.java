package com.philips.research.licensescanner.core.domain.license;

import com.philips.research.licensescanner.core.BusinessException;

public class LicenseException extends BusinessException {
    public LicenseException(String message) {
        super(message);
    }
}
