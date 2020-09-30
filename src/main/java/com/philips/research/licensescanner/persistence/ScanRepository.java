/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

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
    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    Optional<ScanEntity> findTopByPkgOrderByIdDesc(PackageEntity pkg);

    List<ScanEntity> findTop50ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
            Instant from, Instant until);

    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    List<ScanEntity> findAllByPkgAndErrorIsNotNullOrderByTimestampDesc(PackageEntity pkg);

    Optional<ScanEntity> findByUuid(UUID scanId);

    void deleteByPkg(Package pkg);
}
