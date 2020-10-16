/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.LicenseService;

import java.util.stream.Collectors;

abstract class DtoConverter {
    static LicenseService.LicenseDto toDto(Scan scan) {
        final var dto = new LicenseService.LicenseDto();
        dto.timestamp = scan.getTimestamp();
        dto.uuid = scan.getUuid();
        dto.pkg = toPackageId(scan.getPackage());
        dto.license = scan.getLicense().toString();
        dto.error = scan.getError().orElse(null);
        dto.isConfirmed = scan.isConfirmed();
        dto.isContested = scan.isContested();
        dto.location = scan.getLocation().orElse(null);
        dto.detections = scan.getDetections().stream().map(DtoConverter::toDto).collect(Collectors.toList());
        return dto;
    }

    static LicenseService.PackageId toPackageId(Package pkg) {
        final var id = new LicenseService.PackageId();
        id.namespace = pkg.getNamespace();
        id.name = pkg.getName();
        id.version = pkg.getVersion();
        return id;
    }

    private static LicenseService.DetectionDto toDto(Detection detection) {
        final var dto = new LicenseService.DetectionDto();
        dto.license = detection.getLicense().toString();
        dto.file = detection.getFilePath().toString();
        dto.startLine = detection.getStartLine();
        dto.endLine = detection.getEndLine();
        dto.confirmations = detection.getConfirmations();
        return dto;
    }
}
