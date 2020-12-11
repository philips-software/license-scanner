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

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllerExceptionHandlerTest extends AbstractRouteTest {
    private static final String SCAN_URL = "/scans/{uuid}";

    @Test
    void handlesNotFound() throws Exception {
        mockMvc.perform(get(SCAN_URL, SCAN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resource").value(SCAN_ID));
    }
}
