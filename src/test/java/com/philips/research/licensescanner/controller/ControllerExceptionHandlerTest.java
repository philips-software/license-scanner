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

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
class ControllerExceptionHandlerTest extends AbstractRouteTest {
    private static final String PACKAGE_URL = "/packages/{purl}";
    private static final String SCAN_URL = "/scans/{uuid}";

    @Test
    void handlesBadRequest() throws Exception {
        final var response = new JSONObject()
                .put("uuid", "Not A UUID");

        mockMvc.perform(get(SCAN_URL, "Not A UUID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void handlesNotFound() throws Exception {
        final var response = new JSONObject()
                .put("resource", PURL.toString());
        when(service.licenseFor(PURL))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(PACKAGE_URL, encoded(PURL)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(response.toString()));
    }
}
