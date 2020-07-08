package com.philips.research.licensescanner.core.domain.license;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;

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
    String getLicense() {
        return files.stream()
                .flatMap(file -> file.getLicenses().stream())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" AND "));
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

    Collection<String> getLicenses() {
        final Map<String, String> dictionary = new HashMap<>();
        licenses.forEach(lic -> dictionary.put(lic.key, lic.spdx));

        return expressions.stream()
                .map(str -> {
                    var parts = str.split("\\s+");
                    var spdx = Arrays.stream(parts)
                            .map(key -> dictionary.getOrDefault(key, key))
                            .collect(Collectors.joining(" "));
                    return (parts.length <= 1) ? spdx : "(" + spdx + ")";
                })
                .collect(Collectors.toSet());
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
}

