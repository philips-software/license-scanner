package com.philips.research.licensescanner.core;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;

import java.net.URI;
import java.util.Optional;

public interface PackageStore {
    Package createPackage(String namespace, String name, String version);

    Optional<Package> findPackage(String namespace, String name, String version);

    Scan createScan(Package pkg, String license, URI location);

    Optional<Scan> latestScan(Package pkg);
}
