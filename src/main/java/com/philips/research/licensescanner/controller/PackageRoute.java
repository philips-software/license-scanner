/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST API for interacting with packages.
 */
@RestController
@RequestMapping("/packages")
public class PackageRoute extends AbstractRoute {

    @Autowired
    public PackageRoute(LicenseService service) {
        super(service);
    }

    /**
     * Finds all registered packages matching the provided search criteria.
     *
     * @return list of scanning results matching the query
     */
    @GetMapping
    SearchResultJson<ScanInfoJson> findPackages(@RequestParam(required = false, defaultValue = "") String namespace,
                                                @RequestParam(required = false, defaultValue = "") String name,
                                                @RequestParam(required = false, defaultValue = "") String version) {
        final var scans = service.findScans(namespace, name, version);
        final var stats = service.statistics();

        return new SearchResultJson<>(stats, ScanInfoJson.toStream(scans));
    }

    /**
     * Requests licenses for the indicated packages. If the package was scanned before, the result is returned. Else the
     * package is scheduled for scanning.
     *
     * @param body  details where to obtain the package source for scanning
     * @param force forces re-scanning despite an existing scan result
     * @return scan result
     */
    @PostMapping
    ScanInfoJson scanPackage(@RequestBody @Valid ScanRequestJson body,
                             @RequestParam(name = "force", required = false) boolean force) {
        if (force) {
            service.deleteScan(body.purl);
        } else {
            final var scan = service.scanFor(body.purl);
            if (scan.isPresent()) {
                return new ScanInfoJson(scan.get());
            }
        }
        service.scanLicense(body.purl, body.location);

        return new ScanInfoJson(body.purl, body.location);
    }
}

