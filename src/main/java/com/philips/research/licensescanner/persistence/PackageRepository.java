package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PackageRepository extends CrudRepository<PackageEntity, Long> {
    Optional<Package> findByOriginAndNameAndVersion(@Param("origin") String origin, @Param("name") String name, @Param("version") String version);
}
