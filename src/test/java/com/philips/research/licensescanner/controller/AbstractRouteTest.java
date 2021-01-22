/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public abstract class AbstractRouteTest {
    protected static final URI LOCATION = URI.create("git+ssh://example.com@1234");
    protected static final URI PURL = URI.create("pkg:package@version");
    protected static final String SCAN_ID = encoded(PURL);
    protected static final String LICENSE = "MIT OR Apache-2.0";
    protected static final String FILE = "path/to/file";
    protected static final int START_LINE = 12;
    protected static final int END_LINE = 23;
    protected static final int CONFIRMATIONS = 42;

    @MockBean
    protected LicenseService service;

    @Autowired
    protected MockMvc mockMvc;

    protected static String encoded(Object object) {
        return URLEncoder.encode(object.toString(), StandardCharsets.UTF_8);
    }

    @BeforeEach
    void beforeEach() {
        Mockito.reset(service);
    }

    protected LicenseService.ScanDto standardLicenseInfo() {
        final var info = new LicenseService.ScanDto();
        info.purl = PURL;
        info.license = LICENSE;
        info.location = LOCATION;
        return info;
    }

    protected LicenseService.ScanDto standardLicenseInfoWithDetection() {
        final var info = standardLicenseInfo();
        info.detections = new ArrayList<>();
        final var detection = new LicenseService.DetectionDto();
        detection.license = LICENSE;
        detection.file = FILE;
        detection.startLine = START_LINE;
        detection.endLine = END_LINE;
        detection.confirmations = CONFIRMATIONS;
        info.detections.add(detection);
        return info;
    }

    protected JSONObject searchResult(JSONObject... objects) throws Exception {
        var array = new JSONArray();
        Arrays.stream(objects).forEach(array::put);
        return searchResult(array);
    }

    protected JSONObject searchResult(JSONArray array) throws Exception {
        return new JSONObject().put("results", array);
    }
}
