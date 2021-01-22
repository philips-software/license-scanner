/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.persistence;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class PurlConverterTest {
    private static final String VALID = "http://example.com";

    private final PurlConverter converter = new PurlConverter();

    @Test
    void convertsToString() {
        final var string = converter.convertToDatabaseColumn(URI.create(VALID));

        assertThat(string).isEqualTo(VALID);
    }

    @Test
    void convertsToUri() {
        final var uri = converter.convertToEntityAttribute(VALID);

        assertThat(uri.toASCIIString()).isEqualTo(VALID);
    }

    @Test
    void convertsNullValues() {
        //noinspection ConstantConditions
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
