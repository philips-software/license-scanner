/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA converter for storing Licenses as a string.
 */
@Converter(autoApply = true)
@SuppressWarnings({"unused", "RedundantSuppression"})
public class LicenseConverter implements AttributeConverter<License, String> {
    @Override
    public String convertToDatabaseColumn(License license) {
        return license.toString();
    }

    @Override
    public License convertToEntityAttribute(String string) {
        return LicenseParser.parse(string);
    }
}
