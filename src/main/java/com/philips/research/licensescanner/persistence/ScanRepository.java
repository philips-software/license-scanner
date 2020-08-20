package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Scan;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    Optional<Scan> findTopByPkgAndErrorIsNullOrderByIdDesc(PackageEntity pkg);

    List<Scan> findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualAndLicenseNotNullOrderByTimestampDesc(
            Instant from, Instant until);

    List<Scan> findAllByPkgAndErrorIsNotNullOrderByTimestampDesc(PackageEntity pkg);
}
