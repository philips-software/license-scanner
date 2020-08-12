package com.philips.research.licensescanner.core.domain.license.scancode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ScanCode Toolkit JSON result file mapping.
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class ScanCodeJson {
    @JsonProperty("files")
    List<FileJson> files;

    ScanCodeJson() {
    }

    ScanCodeJson(List<FileJson> files) {
        this.files = files;
    }

    /**
     * @return license at are at least 50% certain.
     */
    Collection<License> getLicenses() {
        return files.stream()
                .flatMap(FileJson::getLicenses)
                .collect(Collectors.toSet());
    }
}

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class FileJson {
    @JsonProperty("path")
    String path;
    @JsonProperty("licenses")
    List<LicenseJson> licenses;
    @JsonProperty("license_expressions")
    List<String> expressions;

    FileJson() {
    }

    FileJson(List<LicenseJson> licenses, String... expressions) {
        this.licenses = licenses;
        this.expressions = List.of(expressions);
    }

    Stream<License> getLicenses() {
        final Map<String, String> dictionary = new HashMap<>();
        licenses.forEach(lic -> dictionary.put(lic.key, lic.getSpdxIdentifier()));

        return expressions.stream()
                .map(str -> mapToSpdx(str, dictionary))
                .flatMap(str -> LicenseParser.parse(str).stream());
    }

    private String mapToSpdx(String str, Map<String, String> dictionary) {
        var parts = str.split("\\s+");
        var spdx = Arrays.stream(parts)
                .map(key -> dictionary.getOrDefault(key, key))
                .collect(Collectors.joining(" "));
        return (parts.length <= 1) ? spdx : "(" + spdx + ")";
    }
}

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class LicenseJson {
    @JsonProperty("key")
    String key;
    @JsonProperty("spdx_license_key")
    String spdx;

    LicenseJson() {
    }

    LicenseJson(String key, String spdx) {
        this.key = key;
        this.spdx = spdx;
    }

    public String getSpdxIdentifier() {
        return StringUtils.isEmpty(spdx) ? "Unknown" : spdx;
    }
}

