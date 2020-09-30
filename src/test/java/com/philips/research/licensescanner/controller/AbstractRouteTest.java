/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public abstract class AbstractRouteTest {
    protected static final String NAMESPACE = "Namespace";
    protected static final String NAME = "Name";
    protected static final String VERSION = "Version";
    protected static final URI LOCATION = URI.create("git+ssh://example.com@1234");
    protected static final String LICENSE = "MIT OR Apache-2.0";
    protected static final String FILE = "path/to/file";
    protected static final int START_LINE = 12;
    protected static final int END_LINE = 23;
    protected static final int CONFIRMATIONS = 42;

    @MockBean
    protected LicenseService service;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        Mockito.reset(service);
    }

    protected LicenseService.PackageId standardPackageId() {
        final var id = new LicenseService.PackageId();
        id.namespace = NAMESPACE;
        id.name = NAME;
        id.version = VERSION;
        return id;
    }

    protected LicenseService.LicenseInfo standardLicenseInfo() {
        final var info = new LicenseService.LicenseInfo();
        info.pkg = standardPackageId();
        info.license = LICENSE;
        info.location = LOCATION;
        return info;
    }

    protected LicenseService.LicenseInfo standardLicenseInfoWithDetection() {
        final var info = standardLicenseInfo();
        info.detections = new ArrayList<>();
        final var detection = new LicenseService.DetectionInfo();
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
        return new JSONObject().put("results", array);
    }
}
