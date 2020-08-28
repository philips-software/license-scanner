package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    Optional<ScanEntity> findTopByPkgAndErrorIsNullOrderByIdDesc(PackageEntity pkg);

    List<ScanEntity> findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
            Instant from, Instant until);

    List<ScanEntity> findAllByPkgAndErrorIsNotNullOrderByTimestampDesc(PackageEntity pkg);

    Optional<ScanEntity> findByUuid(UUID scanId);

    void deleteByPkg(Package pkg);
}
