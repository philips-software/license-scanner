package com.philips.research.licensescanner.core.domain;

import java.util.Objects;

public class Package {
    private final String name;
    private final String version;
    public String origin;

    public Package(String origin, String name, String version) {
        this.origin = origin;
        this.name = name;
        this.version = version;
    }

    public String getOrigin() {
        return origin;
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
        return origin.equals(aPackage.origin) &&
                name.equals(aPackage.name) &&
                version.equals(aPackage.version);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(origin, name, version);
    }

    @Override
    public String toString() {
        return origin + ":" + name + "-" + version;
    }
}
