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

import javax.validation.Valid;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
     * @param purl URL-encoded package URL
     * @return scan result
     */
    @GetMapping({"{purl}"})
    ScanInfoJson getLatestScanForPackage(@PathVariable String purl) {
        final URI uri = decodePackageUrl(purl);
        final var license = service.licenseFor(uri)
                .orElseThrow(() -> new ResourceNotFoundException(uri));

        return new ScanInfoJson(license);
    }

    /**
     * Finds all registered packages matching the provided search criteria.
     *
     * @return list of scanning results matching the query
     */
    @GetMapping
    SearchResultJson<URI> findPackages(@RequestParam(required = false, defaultValue = "") String namespace,
                                       @RequestParam(required = false, defaultValue = "") String name,
                                       @RequestParam(required = false, defaultValue = "") String version) {
        final var packages = service.findPackages(namespace, name, version);

        return new SearchResultJson<>(packages.stream());
    }

    /**
     * Requests licenses for the indicated packages. If the package was scanned before, the result is returned. Else the
     * package is scheduled for scanning.
     *
     * @param body  details where to obtain the package source for scanning
     * @param force forces re-scanning despite an existing scan result
     * @return scan result
     */
    @PostMapping("{purl}")
    ScanInfoJson scanPackage(@PathVariable String purl,
                             @Valid @RequestBody ScanRequestJson body,
                             @RequestParam(name = "force", required = false) boolean force) {
        final URI uri = decodePackageUrl(purl);

        if (force) {
            service.deleteScans(uri);
        } else {
            final var license = service.licenseFor(uri);
            if (license.isPresent()) {
                return new ScanInfoJson(license.get());
            }
        }
        service.scanLicense(uri, body.location);

        return new ScanInfoJson(uri, body.location);
    }

    private static URI decodePackageUrl(String purl) {
        try {
            final var decoded = URLDecoder.decode(purl, StandardCharsets.UTF_8);
            return URI.create(decoded);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not decode package URL '" + purl + "'");
        }
    }
}

