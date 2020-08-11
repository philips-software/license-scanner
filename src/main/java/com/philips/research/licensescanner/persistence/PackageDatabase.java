package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Optional;

/**
 * Spring component implementing the persistence of packages.
 */
@Repository
public class PackageDatabase implements PackageStore {
    private final PackageRepository packageRepository;
    private final ScanRepository scanRepository;

    public PackageDatabase(PackageRepository packageRepository, ScanRepository scanRepository) {
        this.packageRepository = packageRepository;
        this.scanRepository = scanRepository;
    }

    @Override
    public Package createPackage(String namespace, String name, String version) {
        final var entity = new PackageEntity(namespace, name, version);
        return packageRepository.save(entity);
    }

    @Override
    public Optional<Package> findPackage(String namespace, String name, String version) {
        return packageRepository.findByNamespaceAndNameAndVersion(namespace, name, version).map(pkg -> pkg);
    }

    @Override
    public Scan createScan(Package pkg, String license, URI location) {
        final var entity = new ScanEntity((PackageEntity) pkg, license, location);
        return scanRepository.save(entity);
    }

    @Override
    public Optional<Scan> latestScan(Package pkg) {
        return scanRepository.findTopByPkgAndLicenseNotNullOrderByIdDesc((PackageEntity) pkg).map(scan -> scan);
    }
}

