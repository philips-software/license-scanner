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
public class PackageRoute {
    private final LicenseService service;

    @Autowired
    public PackageRoute(LicenseService service) {
        this.service = service;
    }

    /**
     * Gets scan results for a single package.
     *
     * @param namespace
     * @param name
     * @param version
     * @return scan result
     */
    @GetMapping({"{name}/{version}", "{namespace}/{name}/{version}"})
    ScanInfoJson getLatestScanForPackage(@PathVariable(required = false) String namespace, @PathVariable String name, @PathVariable String version) {
        if (namespace == null) {
            namespace = "";
        }
        final var license = service.licenseFor(namespace, name, version)
                .orElseThrow(() -> new ResourceNotFoundException("package"));

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
     * @param namespace
     * @param name
     * @param version
     * @param body      details where to obtain the package source for scanning
     * @param force     forces re-scanning despite an existing scan result
     * @return scan result
     */
    @PostMapping({"{name}/{version}", "{namespace}/{name}/{version}"})
    ScanInfoJson scanPackage(@PathVariable(required = false) String namespace, @PathVariable String name, @PathVariable(required = false) String version,
                             @Valid @RequestBody ScanRequestJson body,
                             @RequestParam(name = "force", required = false) boolean force) {
        if (namespace == null) {
            namespace = "";
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

