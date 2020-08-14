package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.ScanError;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Spring component implementing the persistence of packages.
 */
@Repository
public class PackageDatabase implements PackageStore {
    private final PackageRepository packageRepository;
    private final ScanRepository scanRepository;
    private final ScanErrorRepository errorRepository;

    public PackageDatabase(PackageRepository packageRepository, ScanRepository scanRepository, ScanErrorRepository errorRepository) {
        this.packageRepository = packageRepository;
        this.scanRepository = scanRepository;
        this.errorRepository = errorRepository;
    }

    @Override
    public Package createPackage(String namespace, String name, String version) {
        final var entity = new PackageEntity(namespace, name, version);
        return packageRepository.save(entity);
    }

    @Override
    public Optional<Package> getPackage(String namespace, String name, String version) {
        return packageRepository.findByNamespaceAndNameAndVersion(namespace, name, version).map(pkg -> pkg);
    }

    @Override
    public List<Package> findPackages(String namespace, String name, String version) {
        return packageRepository.findTop50ByNamespaceLikeAndNameLikeAndVersionLikeOrderByNamespaceAscNameAscVersionAsc(
                wildcard(namespace), wildcard(name), wildcard(version));
    }

    private String wildcard(String name) {
        return '%' + name + '%';
    }

    @Override
    public Scan createScan(Package pkg, String license, URI location) {
        final var entity = new ScanEntity(Instant.now(), (PackageEntity) pkg, license, location);
        return scanRepository.save(entity);
    }

    @Override
    public Optional<Scan> latestScan(Package pkg) {
        return scanRepository.findTopByPkgAndLicenseNotNullOrderByIdDesc((PackageEntity) pkg);
    }

    @Override
    public void registerScanError(Package pkg, URI location, String message) {
        final var entity = new ScanErrorEntity(Instant.now(), (PackageEntity) pkg, location, message);
        errorRepository.save(entity);
    }

    @Override
    public List<ScanError> scanErrors(Package pkg) {
        return errorRepository.findAllByPkgOrderByTimestampDesc(pkg);
    }

    @Override
    public List<Scan> findScans(Instant from, Instant until) {
        return new ArrayList<>(scanRepository.findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualAndLicenseNotNullOrderByTimestampDesc(from, until));
    }
}

