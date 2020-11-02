/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.philips.research.licensescanner.core.LicenseService;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
class ScanInfoJson {
    final @NullOr UUID id;
    final URI purl;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NullOr Instant timestamp;
    @NullOr URI location;
    @NullOr String license;
    @NullOr String error;
    @NullOr List<DetectionInfoJson> detections;
    boolean contested;
    boolean confirmed;

    public ScanInfoJson(URI purl, @NullOr URI location) {
        this(null, purl, location);
    }

    public ScanInfoJson(@NullOr UUID id, URI purl, @NullOr URI location) {
        this.id = id;
        this.purl = purl;
        this.location = location;
    }

    public ScanInfoJson(LicenseService.LicenseDto info) {
        this(info.uuid, info.purl, info.location);
        timestamp = info.timestamp;
        license = info.license;
        error = info.error;
        contested = info.isContested;
        confirmed = info.isConfirmed;
        if (info.detections != null) {
            this.detections = info.detections.stream()
                    .map(DetectionInfoJson::new)
                    .collect(Collectors.toList());
        }
    }

    static Stream<ScanInfoJson> toStream(List<LicenseService.LicenseDto> licenses) {
        return licenses.stream().map(ScanInfoJson::new);
    }
}

class DetectionInfoJson {
    final String license;
    final String file;
    final int startLine;
    final int endLine;
    final int confirmations;
    final boolean ignored;

    public DetectionInfoJson(LicenseService.DetectionDto info) {
        this.file = info.file;
        this.license = info.license;
        this.startLine = info.startLine;
        this.endLine = info.endLine;
        this.confirmations = info.confirmations;
        this.ignored = info.ignored;
    }
}

class CurationJson {
    @NullOr String license;
}
