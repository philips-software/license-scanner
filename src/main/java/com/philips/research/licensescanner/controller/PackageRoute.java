package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.philips.research.licensescanner.controller.ControllerExceptionHandler.ResourceNotFoundException;

@RestController
@RequestMapping("/package")
public class PackageRoute {
    private final LicenseService service;

    @Autowired
    public PackageRoute(LicenseService service) {
        this.service = service;
    }

    @GetMapping("{namespace}/{name}/{version}")
    ScanInfoJson getPackage(@PathVariable String namespace, @PathVariable String name, @PathVariable String version) {
        final var license = service.licenseFor(namespace, name, version)
                .orElseThrow(() -> new ResourceNotFoundException("package"));

        return withLicenseInfo(new ScanInfoJson(namespace, name, version), license);
    }

    @GetMapping
    SearchResultJson findPackages(@RequestParam String namespace, @RequestParam String name, @RequestParam String version) {
        //TODO Query for provided parameters
        return null;
    }

    @PostMapping("{namespace}/{name}/{version}")
    ScanInfoJson scanPackage(@PathVariable String namespace, @PathVariable String name, @PathVariable(required = false) String version,
                             @Valid @RequestBody ScanRequestJson info,
                             @RequestParam(name = "force", required = false) boolean force) {
        final var response = new ScanInfoJson(namespace, name, version);

        if (!force) {
            final var license = service.licenseFor(namespace, name, version);
            if (license.isPresent()) {
                return withLicenseInfo(response, license.get());
            }
        }

        if (info.location != null) {
            service.scanLicense(namespace, name, version, info.location);
            response.location = info.location;
        }

        return response;
    }

    @PutMapping("{namespace}/{name}/{version}")
    void updatePackage(@PathVariable String namespace, @PathVariable String name, @PathVariable String version, @Valid @RequestBody ScanInfoJson info) {
        //TODO Manually override scan result
    }

    @DeleteMapping("{namespace}/{name}/{version}")
    void deletePackage(@PathVariable String namespace, @PathVariable String name, @PathVariable(required = false) String version) {
        //TODO Manually remove scan results
    }

    private ScanInfoJson withLicenseInfo(ScanInfoJson response, LicenseService.LicenseInfo lic) {
        response.location = lic.location;
        response.licenses = lic.licenses;
        return response;
    }
}

