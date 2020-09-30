/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.validation.Valid;

/**
 * REST API for interacting with packages.
 */
@RestController
@RequestMapping("/packages")
public class PackageRoute {
    private final LicenseService service;

    @Autowired
    public PackageRoute(LicenseService service) {
        this.service = service;
    }

    /**
     * Gets scan results for a single package.
     *
     * @return scan result
     */
    @GetMapping({"{name}/{version}", "{namespace}/{name}/{version}"})
    ScanInfoJson getLatestScanForPackage(@NullOr @PathVariable(required = false) String namespace,
                                         @PathVariable String name,
                                         @PathVariable String version) {
        if (namespace == null) {
            namespace = "";
        }
        if (version.isBlank()) {
            version = "";
        }
        final var resource = String.format("%s/%s/%s", namespace, name, version);
        final var license = service.licenseFor(namespace, name, version)
                .orElseThrow(() -> new ResourceNotFoundException(resource));

        return new ScanInfoJson(license);
    }

    /**
     * Finds all registered packages matching the provided search criteria.
     *
     * @param namespace (Optional) part of the namespace
     * @param name      (Optional) part of the name
     * @param version   (Optional) part of the version
     * @return list of scanning results matching the query
     */
    @GetMapping
    SearchResultJson findPackages(
            @RequestParam(required = false, defaultValue = "") String namespace,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String version) {
        final var packages = service.findPackages(namespace, name, version);

        return new SearchResultJson(packages.stream().map(PackageInfoJson::new));
    }

    /**
     * Requests licenses for the indicated packages. If the package was scanned before, the result is returned. Else the
     * package is scheduled for scanning.
     *
     * @param body  details where to obtain the package source for scanning
     * @param force forces re-scanning despite an existing scan result
     * @return scan result
     */
    @PostMapping({"{name}/{version}", "{namespace}/{name}/{version}"})
    ScanInfoJson scanPackage(@NullOr @PathVariable(required = false) String namespace,
                             @PathVariable String name,
                             @PathVariable String version,
                             @Valid @RequestBody ScanRequestJson body,
                             @RequestParam(name = "force", required = false) boolean force) {
        if (namespace == null) {
            namespace = "";
        }
        if (version.isBlank()) {
            version = "";
        }

        if (force) {
            service.deleteScans(namespace, name, version);
        } else {
            final var license = service.licenseFor(namespace, name, version);
            if (license.isPresent()) {
                return new ScanInfoJson(license.get());
            }
        }
        service.scanLicense(namespace, name, version, body.location);

        return new ScanInfoJson(namespace, name, version, body.location);
    }
}

