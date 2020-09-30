/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain;

import java.util.Objects;

/**
 * A (potential) external dependency of a product.
 */
public class Package {
    private final String namespace;
    private final String name;
    private final String version;

    public Package(String namespace, String name, String version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Package)) return false;
        Package aPackage = (Package) o;
        return namespace.equals(aPackage.namespace) &&
                name.equals(aPackage.name) &&
                version.equals(aPackage.version);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(namespace, name, version);
    }

    @Override
    public String toString() {
        return namespace + ":" + name + "-" + version;
    }
}
