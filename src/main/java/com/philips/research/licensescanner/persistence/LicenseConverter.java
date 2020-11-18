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

import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA converter for storing Licenses as a string.
 */
@Converter(autoApply = true)
@SuppressWarnings({"unused", "RedundantSuppression"})
public class LicenseConverter implements AttributeConverter<License, String> {
    @Override
    public @NullOr String convertToDatabaseColumn(@NullOr License license) {
        return (license != null) ? license.toString() : null;
    }

    @Override
    public @NullOr License convertToEntityAttribute(@NullOr String string) {
        return (string != null) ? LicenseParser.parse(string) : null;
    }
}
