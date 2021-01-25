/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    Optional<ScanEntity> findFirstByPurlOrderByTimestampDesc(URI purl);

    List<ScanEntity> findTop50BySearchLikeIgnoreCaseOrderByPurlAsc(String mask);

    List<ScanEntity> findTop100ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
            Instant from, Instant until);

    List<ScanEntity> findFirst100ByErrorIsNotNullOrderByTimestampDesc();

    List<ScanEntity> findFirst100ByContestingIsNotNullOrderByTimestampDesc();

    int countByErrorIsNull();

    int countByErrorIsNotNull();

    int countByContestingIsNotNull();
}
