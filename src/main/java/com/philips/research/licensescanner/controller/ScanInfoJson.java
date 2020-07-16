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
    URI location;
    List<String> licenses = new ArrayList<>();

    public ScanInfoJson(String namespace, String name, String version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }
}
