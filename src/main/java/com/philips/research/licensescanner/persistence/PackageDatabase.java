package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PackageDatabase implements PackageStore {
    private final PackageRepository packageRepository;

    public PackageDatabase(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @Override
    public Package createPackage(String origin, String name, String version) {
        final var entity = new PackageEntity(origin, name, version);
        packageRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Package> findPackage(String origin, String name, String version) {
        return packageRepository.findByOriginAndNameAndVersion(origin, name, version);
    }

    @Override
    public void createScan(Scan scan) {

    }
}

