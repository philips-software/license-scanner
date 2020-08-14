package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
class ScanInfoJson {
    final String namespace;
    final String name;
    final String version;
    URI location;
    List<String> licenses;
    String error;

    public ScanInfoJson(LicenseService.LicenseInfo info) {
        this(info.namespace, info.name, info.version);
        location = info.location;
        licenses = info.licenses;
    }

    public ScanInfoJson(LicenseService.PackageId pkg) {
        this(pkg.namespace, pkg.name, pkg.version);
    }

    public ScanInfoJson(String namespace, String name, String version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }
}
