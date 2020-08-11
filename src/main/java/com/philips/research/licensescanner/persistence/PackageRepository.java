package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring JPA query definitions for packages.
 */
interface PackageRepository extends CrudRepository<PackageEntity, Long> {
    Optional<PackageEntity> findByNamespaceAndNameAndVersion(String namespace, String name, String version);
}
