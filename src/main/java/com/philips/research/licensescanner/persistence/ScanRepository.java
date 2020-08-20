package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    Optional<ScanEntity> findTopByPkgAndErrorIsNullOrderByIdDesc(PackageEntity pkg);

    List<ScanEntity> findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualAndLicenseNotNullOrderByTimestampDesc(
            Instant from, Instant until);

    List<ScanEntity> findAllByPkgAndErrorIsNotNullOrderByTimestampDesc(PackageEntity pkg);
}
