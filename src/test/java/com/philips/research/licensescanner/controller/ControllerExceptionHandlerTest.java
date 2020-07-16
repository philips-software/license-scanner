package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
class ControllerExceptionHandlerTest {
    private static final String ORIGIN = "Origin";
    private static final String PACKAGE = "Package";
    private static final String VERSION = "Version";
    private static final String PACKAGE_URL = "/package/{origin}/{package}/{version}";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LicenseService service;

    @Test
    void handlesBadRequest() throws Exception {
        final var response = new JSONObject()
                .put("location", "must not be null");

        mockMvc.perform(post(PACKAGE_URL, ORIGIN, PACKAGE, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response.toString()));
    }

    @Test
    void handlesNotFound() throws Exception {
        final var response = new JSONObject()
                .put("resource", "package");
        when(service.licenseFor(ORIGIN, PACKAGE, VERSION))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(PACKAGE_URL, ORIGIN, PACKAGE, VERSION))
                .andExpect(status().isNotFound())
                .andExpect(content().json(response.toString()));
    }
}
