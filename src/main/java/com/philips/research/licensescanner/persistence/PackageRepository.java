/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring JPA query definitions for packages.
 */
interface PackageRepository extends CrudRepository<PackageEntity, Long> {
    Optional<PackageEntity> findByNamespaceAndNameAndVersion(String namespace, String name, String version);

    List<Package> findTop50ByNamespaceLikeAndNameLikeAndVersionLikeOrderByNamespaceAscNameAscVersionAsc(
            @Param("namespace") String namespace, @Param("name") String name, @Param("version") String version);
}
