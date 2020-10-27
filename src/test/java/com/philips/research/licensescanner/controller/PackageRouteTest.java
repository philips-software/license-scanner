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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PackageRouteTest extends AbstractRouteTest {
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final String BASE_URL = "/packages";
    private static final String PACKAGE_URL = BASE_URL + "/{purl}";

    @Nested
    class FindPackages {
        @Test
        void findsPackageByAllFields() throws Exception {
            final var response = searchResult(new JSONArray().put( PURL));
            when(service.findPackages(NAMESPACE, NAME, VERSION))
                    .thenReturn(List.of(PURL));

            mockMvc.perform(get(BASE_URL + "?namespace={ns}&name={name}&version={version}", NAMESPACE, NAME, VERSION))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }

        @Test
        void findsPackagesByOptionalFields() throws Exception {
            final var response = searchResult(new JSONArray().put(PURL));
            when(service.findPackages("", "", ""))
                    .thenReturn(List.of(PURL));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }
    }

    @Nested
    class ScanResults {
        @Test
        void getsExistingScanResult() throws Exception {
            final var response = new JSONObject()
                    .put("license", LICENSE)
                    .put("detections", new JSONArray().put(new JSONObject().put("file", FILE)));
            when(service.licenseFor(PURL))
                    .thenReturn(Optional.of(standardLicenseInfoWithDetection()));

            mockMvc.perform(get(PACKAGE_URL, encoded(PURL)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }

        @Test
        void notFound_getUnknownPackage() throws Exception {
            when(service.licenseFor(PURL))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get(PACKAGE_URL, encoded(PURL)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void returnsEarlierScanResult_scannedBefore() throws Exception {
            final var body = new JSONObject().put("location", "http://somewhere.com/else");
            final var response = new JSONObject().put("location", LOCATION);
            when(service.licenseFor(PURL))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL, encoded(PURL))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service, never()).scanLicense(PURL, LOCATION);
        }

        @Test
        void schedulesNewScan_noScanExists() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);
            final var response = new JSONObject().put("purl", PURL.toString());

            mockMvc.perform(post(PACKAGE_URL, encoded(PURL))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service).scanLicense(PURL, LOCATION);
        }

        @Test
        void forcesRescanning() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);
            final var response = new JSONObject().put("purl", PURL.toString());
            when(service.licenseFor(PURL))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL + "?force=yes", encoded(PURL))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service).deleteScans(PURL);
            verify(service).scanLicense(PURL, LOCATION);
        }
    }
}
