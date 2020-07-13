package com.philips.research.licensescanner.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface PackageRepository extends CrudRepository<PackageEntity, Long> {
    Optional<PackageEntity> findByOriginAndNameAndVersion(@Param("origin") String origin, @Param("name") String name, @Param("version") String version);
}
