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

    @GetMapping("{origin}/{pkg}/{version}")
    ScanInfoJson getPackage(@PathVariable String origin, @PathVariable String pkg, @PathVariable String version) {
        final var license = service.licenseFor(origin, pkg, version)
                .orElseThrow(() -> new ResourceNotFoundException("package"));

        return withLicenseInfo(new ScanInfoJson(origin, pkg, version), license);
    }

    @GetMapping
    SearchResultJson findPackages(@RequestParam String origin, @RequestParam("package") String pkg, @RequestParam("version") String version) {
        return null;
    }

    @PostMapping("{origin}/{pkg}/{version}")
    ScanInfoJson scanPackage(@PathVariable String origin, @PathVariable String pkg, @PathVariable String version,
                             @Valid @RequestBody ScanRequestJson info,
                             @RequestParam(name = "force", required = false) boolean force) {
        final var response = new ScanInfoJson(origin, pkg, version);

        if (!force) {
            final var license = service.licenseFor(origin, pkg, version);
            if (license.isPresent()) {
                return withLicenseInfo(response, license.get());
            }
        }

        service.scanLicense(origin, pkg, version, info.vcsUri);

        response.vcsUri = info.vcsUri;
        return response;
    }

    @PutMapping("{origin}/{pkg}/{version}")
    void updatePackage(@PathVariable String origin, @PathVariable String pkg, @PathVariable String version, @Valid @RequestBody ScanInfoJson info) {
    }

    @DeleteMapping("{orogin}/{pkg}/{version}")
    void deletePackage(@PathVariable String origin, @PathVariable String pkg, @PathVariable String version) {

    }

    private ScanInfoJson withLicenseInfo(ScanInfoJson response, LicenseService.LicenseInfo lic) {
        response.vcsUri = lic.location;
        response.license = lic.license;
        return response;
    }
}

