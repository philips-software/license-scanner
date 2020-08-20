package com.philips.research.licensescanner.persistence;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.URI;

/**
 * JPA converter for storing an URI as a String.
 */
@Converter(autoApply = true)
@SuppressWarnings("unused")
class UriConverter implements AttributeConverter<URI, String> {
    @Override
    public String convertToDatabaseColumn(URI uri) {
        return (uri != null) ? uri.toString() : null;
    }

    @Override
    public URI convertToEntityAttribute(String uri) {
        return (uri != null) ? URI.create(uri) : null;
    }
}
