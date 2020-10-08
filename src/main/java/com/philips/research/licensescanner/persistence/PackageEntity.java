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

import com.philips.research.licensescanner.core.domain.Package;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA entity for persisting a package.
 */
@Entity
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "packages")
class PackageEntity extends Package {
    @Id
    @GeneratedValue
    @SuppressWarnings({"unused", "RedundantSuppression"})
    private @NullOr Long id;

    public PackageEntity() {
        super("", "", "");
    }

    PackageEntity(String namespace, String name, String version) {
        super(namespace, name, version);
    }
}
