package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
class ScanInfoJson {
    @JsonProperty("origin")
    final String origin;
    @JsonProperty("package")
    final String pkg;
    @JsonProperty("version")
    final String version;
    @JsonProperty("license")
    String license;
    @JsonProperty("vcsUri")
    URI vcsUri;

    public ScanInfoJson(String origin, String pkg, String version) {
        this.origin = origin;
        this.pkg = pkg;
        this.version = version;
    }
}
