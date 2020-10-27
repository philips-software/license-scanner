/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;
import com.philips.research.licensescanner.core.domain.Package;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Optional<ScanEntity> findTopByPkgOrderByIdDesc(PackageEntity pkg);

    List<ScanEntity> findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
            Instant from, Instant until);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<ScanEntity> findAllByPkgAndErrorIsNotNullOrderByTimestampDesc(PackageEntity pkg);

    Optional<ScanEntity> findByUuid(UUID scanId);

    void deleteByPkg(Package pkg);

    List<ScanEntity> findFirst100ByErrorIsNotNullOrderByTimestampDesc();

    List<ScanEntity> findFirst100ByContestedOrderByTimestampDesc(boolean contested);
}
