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

import java.util.List;
import java.util.Optional;

/**
 * Spring JPA query definitions for packages.
 */
interface PackageRepository extends CrudRepository<PackageEntity, Long> {
    Optional<PackageEntity> findBySlimPurl(String purl);

    List<PackageEntity> findTop50BySlimPurlLikeOrderBySlimPurlAsc(String purl);
}
