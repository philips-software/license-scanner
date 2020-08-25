package com.philips.research.licensescanner.controller;


import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

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
    SearchResultJson latestScans(@RequestParam(required = false) Instant start, @RequestParam(required = false) Instant end) {
        final var scans = service.findScans(
                start != null ? start : Instant.EPOCH,
                end != null ? end : Instant.now());
        return new SearchResultJson(scans.stream().map(ScanInfoJson::new));
    }
}
