package com.philips.research.licensescanner.persistence;

import com.philips.research.licensescanner.core.domain.download.VcsUri;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.URI;

@Converter(autoApply = true)
@SuppressWarnings("unused")
class VcsUriConverter implements AttributeConverter<VcsUri, String> {
    @Override
    public String convertToDatabaseColumn(VcsUri vcsUri) {
        return vcsUri.toString();
    }

    @Override
    public VcsUri convertToEntityAttribute(String uri) {
        return VcsUri.from(URI.create(uri));
    }
}
