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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ScanRouteTest extends AbstractRouteTest {
    private static final UUID SCAN_ID = UUID.randomUUID();
    private static final LicenseService.PackageId PACKAGE_ID = new LicenseService.PackageId();

    private static final String SCANS_URL = "/scans";
    private static final String SCANS_ID_URL = SCANS_URL + "/{uuid}";
    private static final String CONTEST_URL = SCANS_ID_URL + "/contest";

    @BeforeAll
    static void beforeAll() {
        PACKAGE_ID.namespace = "Namespace";
        PACKAGE_ID.name = "Name";
        PACKAGE_ID.version = "Version";
    }

    @Test
    void findsScanById() throws Exception {
        final var response = new JSONObject().put("license", LICENSE);
        when(service.getScan(SCAN_ID)).thenReturn(Optional.of(standardLicenseInfo()));

        mockMvc.perform(get(SCANS_ID_URL, SCAN_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void notFound_scanIdDoesNotExist() throws Exception {
        when(service.getScan(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(SCANS_ID_URL, SCAN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void findsLatestScan() throws Exception {
        final var response = searchResult(new JSONObject().put("license", LICENSE));
        when(service.findScans(eq(Instant.EPOCH), any(Instant.class)))
                .thenReturn(List.of(standardLicenseInfo()));

        mockMvc.perform(get(SCANS_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void findsLatestScansInPeriod() throws Exception {
        final var from = Instant.now();
        final var until = from.plus(Duration.ofHours(3));
        final var response = searchResult(new JSONObject().put("license", LICENSE));
        when(service.findScans(from, until))
                .thenReturn(List.of(standardLicenseInfo()));

        mockMvc.perform(get(SCANS_URL + "?start={from}&end={until}", from, until))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void findsErrors() throws Exception {
        final var dto = new LicenseService.LicenseDto();
        dto.uuid = SCAN_ID;
        dto.pkg = PACKAGE_ID;
        when(service.findErrors()).thenReturn(List.of(dto));

        mockMvc.perform(get(SCANS_URL + "?q=errors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id").exists());
    }

    @Test
    void findsContested() throws Exception {
        final var dto = new LicenseService.LicenseDto();
        dto.uuid = SCAN_ID;
        dto.pkg = PACKAGE_ID;
        when(service.findContested()).thenReturn(List.of(dto));

        mockMvc.perform(get(SCANS_URL + "?q=contested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id").exists());
    }

    @Test
    void contestsScan() throws Exception {
        mockMvc.perform(post(CONTEST_URL, SCAN_ID))
                .andExpect(status().isOk());

        verify(service).contest(SCAN_ID);
    }

    @Test
    void curatesLicense() throws Exception {
        mockMvc.perform(put(SCANS_ID_URL, SCAN_ID)
                .content(new JSONObject().put("license", LICENSE).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).curateLicense(SCAN_ID, LICENSE);
    }
}
