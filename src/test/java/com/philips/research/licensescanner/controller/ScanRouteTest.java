package com.philips.research.licensescanner.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScanRouteTest extends AbstractRouteTest {
    private static final String SCANS_URL = "/scans";

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
}
