/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;


import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.UUID;

/**
 * REST API for interacting with scan results.
 */
@RestController
@RequestMapping("/scans")
public class ScanRoute {
    private final LicenseService service;

    @Autowired
    public ScanRoute(LicenseService service) {
        this.service = service;
    }

    /**
     * Lists all successful scans in the given period.
     *
     * @param start (Optional) start timestamp
     * @param end   (Optional) end timestamp: defaults to "now"
     */
    @GetMapping()
    SearchResultJson latestScans(@NullOr @RequestParam(required = false) Instant start,
                                 @NullOr @RequestParam(required = false) Instant end) {
        final var scans = service.findScans(
                start != null ? start : Instant.EPOCH,
                end != null ? end : Instant.now());

        return new SearchResultJson(scans.stream().map(ScanInfoJson::new));
    }

    @GetMapping("{uuid}")
    ScanInfoJson getScanById(@PathVariable UUID uuid) {
        final var scan = service.getScan(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(uuid));

        return new ScanInfoJson(scan);
    }

    @PutMapping("{uuid}")
    void confirmLicense(@PathVariable UUID uuid, @RequestBody CurationJson body) {
        service.curateLicense(uuid, body.license);
    }

    @PostMapping("{uuid}/contest")
    void contestScan(@PathVariable UUID uuid) {
        service.contest(uuid);
    }

}
