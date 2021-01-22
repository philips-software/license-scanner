/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ControllerExceptionHandler.class, ScanRoute.class, JacksonConfiguration.class})
class ControllerExceptionHandlerTest extends AbstractRouteTest {
    private static final String SCAN_URL = "/scans/{uuid}";

    @Test
    void handlesNotFound() throws Exception {
        mockMvc.perform(get(SCAN_URL, SCAN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resource").value(SCAN_ID));
    }
}
