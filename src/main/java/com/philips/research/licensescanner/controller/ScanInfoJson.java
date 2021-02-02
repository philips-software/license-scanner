/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.philips.research.licensescanner.core.LicenseService;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_NULL)
class ScanInfoJson {
    final String id;
    final URI purl;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NullOr Instant timestamp;
    @NullOr URI location;
    @NullOr String license;
    @NullOr String error;
    @NullOr List<DetectionInfoJson> detections;
    @NullOr String contesting;
    boolean confirmed;

    public ScanInfoJson(URI purl, @NullOr URI location) {
        this.id = encode(purl);
        this.purl = purl;
        this.location = location;
    }

    public ScanInfoJson(LicenseService.ScanDto info) {
        this(info.purl, info.location);
        timestamp = info.timestamp;
        license = info.license;
        error = info.error;
        contesting = info.contesting;
        confirmed = info.isConfirmed;
        if (info.detections != null) {
            this.detections = info.detections.stream()
                    .map(DetectionInfoJson::new)
                    .collect(Collectors.toList());
        }
    }

    static Stream<ScanInfoJson> toStream(List<LicenseService.ScanDto> licenses) {
        return licenses.stream().map(ScanInfoJson::new);
    }

    private static String encode(URI purl) {
        return URLEncoder.encode(URLEncoder.encode(purl.toString(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
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

class LicenseJson {
    @NullOr String license;
}
