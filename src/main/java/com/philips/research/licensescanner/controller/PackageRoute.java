package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;

/**
 * REST API for interacting with packages.
 */
@RestController
@RequestMapping("/package")
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
    @GetMapping("{namespace}/{name}/{version}")
    ScanInfoJson getPackage(@PathVariable String namespace, @PathVariable String name, @PathVariable String version) {
        final var license = service.licenseFor(namespace, name, version)
                .orElseThrow(() -> new ResourceNotFoundException("package"));

        return withLicenseInfo(new ScanInfoJson(namespace, name, version), license);
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

        return new SearchResultJson(packages.stream().map(ScanInfoJson::new));
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
    @PostMapping("{namespace}/{name}/{version}")
    ScanInfoJson scanPackage(@PathVariable String namespace, @PathVariable String name, @PathVariable(required = false) String version,
                             @Valid @RequestBody ScanRequestJson body,
                             @RequestParam(name = "force", required = false) boolean force) {
        final var response = new ScanInfoJson(namespace, name, version);

        if (!force) {
            final var license = service.licenseFor(namespace, name, version);
            if (license.isPresent()) {
                return withLicenseInfo(response, license.get());
            }
        }

        if (body.location != null) {
            service.scanLicense(namespace, name, version, body.location);
            response.location = body.location;
        }

        return response;
    }

    /**
     * Overrides a scan results with (manually corrected) information.
     *
     * @param namespace
     * @param name
     * @param version
     * @param body      Updated scan result
     */
    @PutMapping("{namespace}/{name}/{version}")
    void updatePackage(@PathVariable String namespace, @PathVariable String name, @PathVariable String version, @Valid @RequestBody ScanInfoJson body) {
        //TODO Manually override scan result
    }

    /**
     * Removes a scan result. If no scan result matches the parameters, nothing happens.
     *
     * @param namespace
     * @param name
     * @param version   (optional) version
     */
    @DeleteMapping("{namespace}/{name}/{version}")
    void deletePackage(@PathVariable String namespace, @PathVariable String name, @PathVariable(required = false) String version) {
        //TODO Manually remove scan results
    }

    private ScanInfoJson withLicenseInfo(ScanInfoJson response, LicenseService.LicenseInfo lic) {
        response.location = lic.location;
        response.licenses = lic.licenses;
        return response;
    }

    /**
     * Lists all successful scans in the given period.
     *
     * @param start (Optional) start timestamp
     * @param end   (Optional) end timestamp: defaults to "now"
     */
    @GetMapping("scans")
    SearchResultJson latestScans(@RequestParam(required = false) Instant start, @RequestParam(required = false) Instant end) {
        final var scans = service.findScans(
                start != null ? start : Instant.MIN,
                end != null ? end : Instant.MAX);
        return new SearchResultJson(scans.stream().map(ScanInfoJson::new));
    }

    /**
     * Lists all failed package scans in the given period.
     *
     * @param after  (Optional) start timestamp
     * @param before (Optional) end timestamp; defaults to "now"
     */
    @GetMapping("errors")
    SearchResultJson failedPackages(@RequestParam Instant after, @RequestParam Instant before) {
        // TODO Implement me
        return new SearchResultJson();
    }

    /**
     * Lists all scan errors for a package.
     *
     * @param namespace
     * @param name
     * @param version
     */
    @GetMapping("{namespace}/{name}/{version}/errors")
    SearchResultJson findPackageScanErrors(@PathVariable String namespace, @PathVariable String name, @PathVariable String version) {
        // TODO Implement me
        return new SearchResultJson();
    }
}
