/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.licensescanner.core.LicenseService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

class ScanInfoJsonTest {
    private static final URI LOCATION = URI.create("http://example.com");
    private static final Instant TIMESTAMP = Instant.now();
    private static final String ERROR = "Error";
    private static final URI PURL = URI.create("pkg:package@version");
    private static final String ID = "pkg%253Apackage%2540version";
    private static final String LICENSE = "License";
    private static final ObjectMapper MAPPER = new JacksonConfiguration().objectMapper();

    @Test
    void createsScanInfoJson() throws Exception {
        final var dto = new LicenseService.ScanDto();
        dto.purl = PURL;
        dto.timestamp = TIMESTAMP;
        dto.location = LOCATION;
        dto.contesting = LICENSE;
        dto.isConfirmed = true;
        dto.error = ERROR;
        dto.detections = List.of();
        dto.license = LICENSE;

        final var json = new JSONObject()
                .put("id", ID)
                .put("timestamp", DateTimeFormatter.ISO_INSTANT.format(TIMESTAMP))
                .put("location", LOCATION.toString())
                .put("contesting", LICENSE)
                .put("confirmed", true)
                .put("error", ERROR)
                .put("detections", new JSONArray())
                .put("purl", PURL)
                .put("license", LICENSE);
        JSONAssert.assertEquals(json.toString(), MAPPER.writeValueAsString(new ScanInfoJson(dto)), true);
    }

    @Test
    void createsListOfScanInfo() throws Exception {
        final var dto = new LicenseService.ScanDto();
        dto.purl = PURL;

        final var json = new JSONArray()
                .put(new JSONObject()
                        .put("id", ID));
        JSONAssert.assertEquals(json.toString(),
                MAPPER.writeValueAsString(ScanInfoJson.toStream(List.of(dto)).collect(Collectors.toList())), false);
    }
}
