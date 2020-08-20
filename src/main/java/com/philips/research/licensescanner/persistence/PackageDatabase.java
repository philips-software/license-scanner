package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.PackageStore;
import com.philips.research.licensescanner.core.domain.Package;
import com.philips.research.licensescanner.core.domain.Scan;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring component implementing the persistence of packages.
 */
@Repository
public class PackageDatabase implements PackageStore {
    static DetectionRepository detectionRepository;
    private final PackageRepository packageRepository;
    private final ScanRepository scanRepository;

    public PackageDatabase(PackageRepository packageRepository, ScanRepository scanRepository,
                           DetectionRepository detectionRepository) {
        this.packageRepository = packageRepository;
        this.scanRepository = scanRepository;
        PackageDatabase.detectionRepository = detectionRepository;
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
    public Scan createScan(Package pkg, URI location) {
        final var entity = new ScanEntity(Instant.now(), (PackageEntity) pkg, location);
        return scanRepository.save(entity);
    }

    @Override
    public Optional<Scan> latestScan(Package pkg) {
        return scanRepository.findTopByPkgAndErrorIsNullOrderByIdDesc((PackageEntity) pkg);
    }

    @Override
    public void deleteScan(Scan scan) {
        scanRepository.delete((ScanEntity) scan);
    }

    @Override
    public List<Scan> scanErrors(Package pkg) {
        return scanRepository.findAllByPkgAndErrorIsNotNullOrderByTimestampDesc((PackageEntity) pkg);
    }

    @Override
    public List<Scan> findScans(Instant from, Instant until) {
        return scanRepository.findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualAndLicenseNotNullOrderByTimestampDesc(from, until);
    }
}

