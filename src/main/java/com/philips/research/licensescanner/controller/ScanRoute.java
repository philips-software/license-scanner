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

import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;

/**
 * REST API for interacting with scan results.
 */
@RestController
@RequestMapping("/scans")
public class ScanRoute extends AbstractRoute {

    @Autowired
    public ScanRoute(LicenseService service) {
        super(service);
    }

    /**
     * Lists all successful scans in the given period.
     *
     * @param start (Optional) start timestamp
     * @param end   (Optional) end timestamp: defaults to "now"
     */
    @GetMapping()
    SearchResultJson<ScanInfoJson> latestScans(@NullOr @RequestParam(required = false) Instant start,
                                               @NullOr @RequestParam(required = false) Instant end,
                                               @RequestParam(name = "q", required = false, defaultValue = "") String query) {
        final var stats = service.statistics();

        if (query.startsWith("error")) {
            return new SearchResultJson<>(stats, ScanInfoJson.toStream(service.findErrors()));
        }
        if (query.startsWith("contest")) {
            return new SearchResultJson<>(stats, ScanInfoJson.toStream(service.findContested()));
        }
        final var scans = service.findScans(
                start != null ? start : Instant.EPOCH,
                end != null ? end : Instant.now());

        return new SearchResultJson<>(stats, ScanInfoJson.toStream(scans));
    }

    @GetMapping("{id}")
    ScanInfoJson getScanById(@PathVariable String id) {
        final var purl = decodePackageUrl(id);
        final var scan = service.getScan(purl)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        return new ScanInfoJson(scan);
    }

    @PutMapping("{id}")
    void confirmLicense(@PathVariable String id, @RequestBody LicenseJson body) {
        final var purl = decodePackageUrl(id);
        service.curateLicense(purl, body.license);
    }

    @DeleteMapping("{id}")
    void deleteScan(@PathVariable String id) {
        final var purl = decodePackageUrl(id);
        service.deleteScan(purl);
    }

    @PostMapping("{id}/contest")
    void contestScan(@PathVariable String id, @RequestBody(required = false) @NullOr LicenseJson body) {
        final URI purl = decodePackageUrl(id);
        final @NullOr String license = (body != null) ? body.license : null;

        service.contest(purl, license);
    }

    @PostMapping("{id}/ignore/{license}")
    void ignoreDetection(@PathVariable String id, @PathVariable String license,
                         @RequestParam(required = false) boolean revert) {
        final URI purl = decodePackageUrl(id);

        if (!revert) {
            service.ignore(purl, license);
        } else {
            service.restore(purl, license);
        }
    }

    @GetMapping("{id}/source/{license}")
    FragmentJson detectionSource(@PathVariable String id, @PathVariable String license,
                                 @RequestParam(required = false, defaultValue = "5") int margin) {
        final var purl = decodePackageUrl(id);
        final var dto = service.sourceFragment(purl, license, margin)
                .orElseThrow(() -> new ResourceNotFoundException("" + id + "/" + license));
        return new FragmentJson(dto);
    }
}
