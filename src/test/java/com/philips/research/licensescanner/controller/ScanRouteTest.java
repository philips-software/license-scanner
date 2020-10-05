/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import org.json.JSONObject;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScanRouteTest extends AbstractRouteTest {
    private static final UUID SCAN_ID = UUID.randomUUID();

    private static final String SCANS_URL = "/scans";
    private static final String SCANS_ID_URL = SCANS_URL + "/{uuid}";
    private static final String CONTEST_URL = SCANS_ID_URL + "/contest";

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
