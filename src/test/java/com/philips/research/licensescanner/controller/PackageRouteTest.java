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
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PackageRouteTest extends AbstractRouteTest {
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String PACKAGE_URL = "/packages";

    @Nested
    class Scanning {
        @Test
        void badRequest_missingPurl() throws Exception {
            mockMvc.perform(post(PACKAGE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returnsEarlierScanResult_scannedBefore() throws Exception {
            final var body = new JSONObject()
                    .put("purl", PURL)
                    .put("location", LOCATION);
            when(service.scanFor(PURL))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.license").value(LICENSE));

            verify(service, never()).scanLicense(PURL, LOCATION);
        }

        @Test
        void schedulesNewScan_noScanExists() throws Exception {
            final var body = new JSONObject().put("purl", PURL).put("location", LOCATION);

            mockMvc.perform(post(PACKAGE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.license").doesNotExist())
                    .andExpect(jsonPath("$.location").value(LOCATION.toString()));

            verify(service).scanLicense(PURL, LOCATION);
        }

        @Test
        void forcesRescanning() throws Exception {
            final var body = new JSONObject().put("purl", PURL).put("location", LOCATION);
            when(service.scanFor(PURL))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL + "?force=yes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.purl").value(PURL.toString()));

            verify(service).deleteScan(PURL);
            verify(service).scanLicense(PURL, LOCATION);
        }
    }

    @Nested
    class FindPackages {
        final LicenseService.ScanDto scan = new LicenseService.ScanDto();

        @BeforeEach()
        void beforeEach() {
            scan.purl = PURL;
            when(service.statistics()).thenReturn(new LicenseService.StatisticsDto());
        }

        @Test
        void findsPackageByAllFields() throws Exception {
            when(service.findScans(NAMESPACE, NAME, VERSION))
                    .thenReturn(List.of(scan));

            mockMvc.perform(get(PACKAGE_URL + "?namespace={ns}&name={name}&version={version}", NAMESPACE, NAME, VERSION))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results[0].purl").value(PURL.toString()));
        }

        @Test
        void findsPackagesByOptionalFields() throws Exception {
            when(service.findScans("", "", ""))
                    .thenReturn(List.of(scan));

            mockMvc.perform(get(PACKAGE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results[0].purl").value(PURL.toString()));
        }
    }

}
