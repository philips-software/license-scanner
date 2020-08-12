package com.philips.research.licensescanner.core.domain.license;

/**
 * Copy and license rights.
 */
public class Copyright {
    private final License license;

    public Copyright(License license) {
        this.license = license;
    }

    public License getLicense() {
        return license;
    }
}
