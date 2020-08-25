package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.philips.research.licensescanner.core.LicenseService;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
class ScanInfoJson extends PackageInfoJson {
    UUID uuid;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant timestamp;
    URI location;
    String license;
    String error;
    List<DetectionInfoJson> detections;
    boolean isContested;
    boolean isConfirmed;

    public ScanInfoJson(String namespace, String name, String version, URI location) {
        super(namespace, name, version);
        this.location = location;
    }

    public ScanInfoJson(LicenseService.LicenseInfo info) {
        super(info.pkg);
        uuid = info.uuid;
        timestamp = info.timestamp;
        location = info.location;
        license = info.license;
        error = info.error;
        isContested = info.isContested;
        isConfirmed = info.isConfirmed;
        if (info.detections != null) {
            this.detections = info.detections.stream()
                    .map(DetectionInfoJson::new)
                    .collect(Collectors.toList());
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class PackageInfoJson {
    final String namespace;
    final String name;
    final String version;

    public PackageInfoJson(LicenseService.PackageId id) {
        this(id.namespace, id.name, id.version);
    }

    public PackageInfoJson(String namespace, String name, String version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }
}

class DetectionInfoJson {
    final String file;
    final String license;
    final int startLine;
    final int endLine;
    final int confirmations;

    public DetectionInfoJson(LicenseService.DetectionInfo info) {
        this.file = info.file;
        this.license = info.license;
        this.startLine = info.startLine;
        this.endLine = info.endLine;
        this.confirmations = info.confirmations;
    }
}
