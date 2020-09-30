/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
    private static final String BASE_URL = "/packages";
    private static final String PACKAGE_URL = BASE_URL + "/{origin}/{pkg}/{version}";

    @Nested
    class FindPackages {
        @Test
        void findsPackageByAllFields() throws Exception {
            final var response = searchResult(new JSONObject().put("name", NAME));
            when(service.findPackages(NAMESPACE, NAME, VERSION))
                    .thenReturn(List.of(standardPackageId()));

            mockMvc.perform(get(BASE_URL + "?namespace={ns}&name={name}&version={version}", NAMESPACE, NAME, VERSION))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }

        @Test
        void findsPackagesByOptionalFields() throws Exception {
            final var response = searchResult(new JSONObject().put("name", NAME));
            when(service.findPackages("", "", ""))
                    .thenReturn(List.of(standardPackageId()));

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
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.of(standardLicenseInfoWithDetection()));

            mockMvc.perform(get(PACKAGE_URL, NAMESPACE, NAME, VERSION))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }

        @Test
        void getsExistingScanResultForEmptyNamespace() throws Exception {
            final var info = standardLicenseInfo();
            info.pkg.namespace = "";
            when(service.licenseFor(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.of(info));

            mockMvc.perform(get(PACKAGE_URL, "", NAME, VERSION))
                    .andExpect(status().isOk());

            verify(service).licenseFor("", NAME, VERSION);
        }

        @Test
        void notFound_getUnknownPackage() throws Exception {
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get(PACKAGE_URL, NAMESPACE, NAME, VERSION))
                    .andExpect(status().isNotFound());
        }

        @Test
        void returnsEarlierScanResult_scannedBefore() throws Exception {
            final var body = new JSONObject().put("location", "http://somewhere.com/else");
            final var response = new JSONObject().put("location", LOCATION);
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL, NAMESPACE, NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service, never()).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
        }

        @Test
        void scansPackagesWithoutNamespace() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);

            mockMvc.perform(post(PACKAGE_URL, "", NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk());

            verify(service).scanLicense("", NAME, VERSION, LOCATION);
        }

        @Test
        void schedulesNewScan_noScanExists() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);
            final var response = new JSONObject().put("name", NAME);

            mockMvc.perform(post(PACKAGE_URL, NAMESPACE, NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
        }

        @Test
        void forcesRescanning() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);
            final var response = new JSONObject().put("name", NAME);
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL + "?force=yes", NAMESPACE, NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service).deleteScans(NAMESPACE, NAME, VERSION);
            verify(service).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
        }
    }
}
