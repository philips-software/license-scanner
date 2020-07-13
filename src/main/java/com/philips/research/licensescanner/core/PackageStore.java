package com.philips.research.licensescanner.core;

import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.download.VcsUri;

import java.util.Optional;

public interface PackageStore {
    Package createPackage(String origin, String pkg, String version);

    Optional<Package> findPackage(String origin, String pkg, String version);

    Scan createScan(Package pkg, String license, VcsUri vcsUri);

    Optional<Scan> latestScan(Package pkg);
}
