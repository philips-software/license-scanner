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

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring JPA query definitions for scans.
 */
interface ScanRepository extends CrudRepository<ScanEntity, Long> {
    Optional<ScanEntity> findByPurl(URI purl);

    List<ScanEntity> findTop50BySearchLikeOrderByPurlAsc(String mask);

    List<ScanEntity> findTop100ByTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
            Instant from, Instant until);

    List<ScanEntity> findFirst100ByErrorIsNotNullOrderByTimestampDesc();

    List<ScanEntity> findFirst100ByContestingIsNotNullOrderByTimestampDesc();

    int countByErrorIsNull();

    int countByErrorIsNotNull();

    int countByContestingIsNotNull();
}
