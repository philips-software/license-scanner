package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    Optional<ScanEntity> findTopByPkgAndLicenseNotNullOrderByIdDesc(PackageEntity pkg);
}
