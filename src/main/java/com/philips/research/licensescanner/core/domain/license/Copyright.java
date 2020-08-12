package com.philips.research.licensescanner.core.domain.license;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copy and license rights.
 */
public class Copyright {
    private final Set<License> licenses = new HashSet<>();

    public Collection<License> getLicenses() {
        return licenses;
    }

    public Copyright addLicense(License license) {
        licenses.add(license);
        return this;
    }
}
