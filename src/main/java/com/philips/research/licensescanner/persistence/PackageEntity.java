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

import pl.tlinkowski.annotation.basic.NullOr;

import javax.persistence.*;
import java.net.URI;
import com.philips.research.licensescanner.core.domain.Package;

/**
 * JPA entity for persisting a package.
 */
@Entity
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(name = "packages")
class PackageEntity extends Package {
    private static final URI EMPTY_URI = URI.create("");

    @Id
    @GeneratedValue
    @SuppressWarnings({"unused", "RedundantSuppression"})
    private @NullOr Long id;
    @Column(name = "purl")
    @Lob
    private final String slimPurl;

    public PackageEntity() {
        this(EMPTY_URI);
    }

    PackageEntity(URI purl) {
        super(EMPTY_URI);
        this.slimPurl = toSlim(purl);
    }

    static String toSlim(URI purl) {
        return purl.toString().replaceFirst("^pkg:", "");
    }

    @Override
    public URI getPurl() {
        return URI.create("pkg:" + slimPurl);
    }
}
