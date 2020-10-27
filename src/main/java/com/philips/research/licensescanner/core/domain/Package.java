/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain;

import java.net.URI;
import java.util.Objects;

public class Package {
    private final URI purl;

    public Package(URI purl) {
        this.purl = purl;
    }

    public URI getPurl() {
        return purl;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Package)) return false;
        Package aPackage = (Package) o;
        return getPurl().equals(aPackage.getPurl());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getPurl());
    }

    @Override
    public String toString() {
        return this.getClass() + ":" + getPurl().toString();
    }
}
