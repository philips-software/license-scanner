/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import com.philips.research.licensescanner.core.LicenseService.StatisticsDto;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ScanRoute.class, JacksonConfiguration.class})
class ScanRouteTest extends AbstractRouteTest {
    private static final String SCANS_URL = "/scans";
    private static final String SCAN_URL = SCANS_URL + "/{id}";
    private static final String CONTEST_URL = SCAN_URL + "/contest";
    private static final String IGNORE_DETECTION_URL = SCAN_URL + "/ignore/{license}";
    private static final String DETECTION_SOURCE_URL = SCAN_URL + "/source/{license}";

    @Test
    void findsScanById() throws Exception {
        final var response = new JSONObject().put("license", LICENSE);
        when(service.getScan(PURL)).thenReturn(Optional.of(standardLicenseInfo()));

        mockMvc.perform(get(SCAN_URL, SCAN_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void badRequest_malformedScanId() throws Exception {
        mockMvc.perform(get(SCAN_URL, "Not an id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notFound_scanIdDoesNotExist() throws Exception {
        when(service.getScan(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(SCAN_URL, SCAN_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void findsLatestScan() throws Exception {
        final var response = searchResult(new JSONObject().put("license", LICENSE));
        when(service.statistics()).thenReturn(new StatisticsDto());
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
        when(service.statistics()).thenReturn(new StatisticsDto());
        when(service.findScans(from, until))
                .thenReturn(List.of(standardLicenseInfo()));

        mockMvc.perform(get(SCANS_URL + "?start={from}&end={until}", from, until))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void findsErrors() throws Exception {
        final var dto = new LicenseService.ScanDto();
        dto.purl = PURL;
        when(service.statistics()).thenReturn(new StatisticsDto());
        when(service.findErrors()).thenReturn(List.of(dto));

        mockMvc.perform(get(SCANS_URL + "?q=errors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id").exists());
    }

    @Test
    void findsContested() throws Exception {
        final var dto = new LicenseService.ScanDto();
        dto.purl = PURL;
        when(service.statistics()).thenReturn(new StatisticsDto());
        when(service.findContested()).thenReturn(List.of(dto));

        mockMvc.perform(get(SCANS_URL + "?q=contested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id").exists());
    }

    @Test
    void contestsScan() throws Exception {
        mockMvc.perform(post(CONTEST_URL, SCAN_ID))
                .andExpect(status().isOk());

        verify(service).contest(PURL, null);
    }

    @Test
    void contestsScanWithSuggestion() throws Exception {
        final var json = new JSONObject().put("license", LICENSE);

        mockMvc.perform(post(CONTEST_URL, SCAN_ID)
                .content(json.toString()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).contest(PURL, LICENSE);
    }

    @Test
    void curatesLicense() throws Exception {
        mockMvc.perform(put(SCAN_URL, SCAN_ID)
                .content(new JSONObject().put("license", LICENSE).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).curateLicense(PURL, LICENSE);
    }

    @Test
    void ignoresFalsePositiveDetection() throws Exception {
        mockMvc.perform(post(IGNORE_DETECTION_URL, SCAN_ID, LICENSE))
                .andExpect(status().isOk());

        verify(service).ignore(PURL, LICENSE);
    }

    @Test
    void undoesFalsePositiveDetection() throws Exception {
        mockMvc.perform(post(IGNORE_DETECTION_URL + "?revert=yes", SCAN_ID, LICENSE))
                .andExpect(status().isOk());

        verify(service).restore(PURL, LICENSE);
    }

    @Test
    void readsDetectionSource() throws Exception {
        final var dto = new LicenseService.FileFragmentDto();
        dto.filename = FILE;
        when(service.sourceFragment(PURL, LICENSE, 7)).thenReturn(Optional.of(dto));

        mockMvc.perform(get(DETECTION_SOURCE_URL + "?margin=7", SCAN_ID, LICENSE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.file").value(FILE));
    }

    @Test
    void notFound_noDetectionSource() throws Exception {
        mockMvc.perform(get(DETECTION_SOURCE_URL, SCAN_ID, LICENSE))
                .andExpect(status().isNotFound());

        verify(service).sourceFragment(PURL, LICENSE, 5);
    }

    @Test
    void deletesScan() throws Exception {
        mockMvc.perform(delete(SCAN_URL, SCAN_ID))
                .andExpect(status().isOk());

        verify(service).deleteScan(PURL);
    }
}
