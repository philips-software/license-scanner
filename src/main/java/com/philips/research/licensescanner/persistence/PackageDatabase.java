package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.download.VcsUri;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PackageDatabase implements PackageStore {
    private final PackageRepository packageRepository;
    private final ScanRepository scanRepository;

    public PackageDatabase(PackageRepository packageRepository, ScanRepository scanRepository) {
        this.packageRepository = packageRepository;
        this.scanRepository = scanRepository;
    }

    @Override
    public Package createPackage(String origin, String name, String version) {
        final var entity = new PackageEntity(origin, name, version);
        return packageRepository.save(entity);
    }

    @Override
    public Optional<Package> findPackage(String origin, String name, String version) {
        return packageRepository.findByOriginAndNameAndVersion(origin, name, version);
    }

    @Override
    public Scan createScan(Package pkg, String license, VcsUri vcsUri) {
        final var entity = new ScanEntity(pkg, license, vcsUri);
        return scanRepository.save(entity);
    }

    @Override
    public Scan latestScan(Package pkg) {
        return scanRepository.findAll().iterator().next();
    }
}

