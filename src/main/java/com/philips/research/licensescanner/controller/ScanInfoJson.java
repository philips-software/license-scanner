package com.philips.research.licensescanner.controller;

import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Validated
class ScanInfoJson {
    final String namespace;
    final String name;
    final String version;
    List<String> licenses = new ArrayList<>();
    URI location;

    public ScanInfoJson(String namespace, String name, String version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }
}
