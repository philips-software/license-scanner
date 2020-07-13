package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.Package;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "packages")
public class PackageEntity extends Package {
    @Id
    @GeneratedValue
    public Long id;

    public PackageEntity() {
        super(null, null, null);
    }

    PackageEntity(String origin, String name, String version) {
        super(origin, name, version);
    }
}
