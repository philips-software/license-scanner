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

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.File;

/**
 * JPA converter for storing a file path as a String.
 */
@Converter(autoApply = true)
@SuppressWarnings({"unused", "RedundantSuppression"})
class FileConverter implements AttributeConverter<File, String> {
    @Override
    public String convertToDatabaseColumn(File file) {
        return file.getPath();
    }

    @Override
    public File convertToEntityAttribute(String path) {
        return new File(path);
    }
}
