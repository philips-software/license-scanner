package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * JPA entity for persisting a package.
 */
@Entity(name = "packages")
class PackageEntity extends Package {
    @Id
    @GeneratedValue
    private Long id;

    public PackageEntity() {
        super(null, null, null);
    }

    PackageEntity(String namespace, String name, String version) {
        super(namespace, name, version);
    }
}
