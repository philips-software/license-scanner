package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.json.JSONArray;
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

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
class PackageRouteTest {
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";
    private static final URI LOCATION = URI.create("git+ssh://example.com@1234");
    private static final String LICENSE = "MIT OR Apache-2.0";
    private static final String PACKAGE_URL = "/package/{origin}/{pkg}/{version}";

    @MockBean
    LicenseService service;

    @Autowired
    private MockMvc mockMvc;

    private JSONObject packageInfoJson() throws Exception {
        return new JSONObject()
                .put("namespace", NAMESPACE)
                .put("name", NAME)
                .put("version", VERSION)
                .put("location", LOCATION);
    }

    @Test
    void getsExistingScanResult() throws Exception {
        final var response = packageInfoJson().put("licenses", new JSONArray().put(LICENSE));
        when(service.licenseFor(NAMESPACE, NAME, VERSION))
                .thenReturn(Optional.of(new LicenseService.LicenseInfo(LOCATION, List.of(LICENSE))));

        mockMvc.perform(get(PACKAGE_URL, NAMESPACE, NAME, VERSION))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString(), true));
    }

    @Test
    void notFound_getUnknownPackage() throws Exception {
        when(service.licenseFor(NAMESPACE, NAME, VERSION))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(PACKAGE_URL, NAMESPACE, NAME, VERSION))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsEarlierScanResult_scannedBefore() throws Exception {
        final var body = new JSONObject().put("location", LOCATION);
        final var response = packageInfoJson()
                .put("licenses", new JSONArray().put(LICENSE));
        when(service.licenseFor(NAMESPACE, NAME, VERSION))
                .thenReturn(Optional.of(new LicenseService.LicenseInfo(LOCATION, List.of(LICENSE))));

        mockMvc.perform(post(PACKAGE_URL, NAMESPACE, NAME, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString(), true));

        verify(service, never()).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
    }

    @Test
    void schedulesNewScan_noScanExists() throws Exception {
        final var body = new JSONObject().put("location", LOCATION);
        final var response = packageInfoJson();

        mockMvc.perform(post(PACKAGE_URL, NAMESPACE, NAME, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));

        verify(service).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
    }

    @Test
    void forcesRescanning() throws Exception {
        final var body = new JSONObject().put("location", LOCATION);
        final var response = packageInfoJson();
        when(service.licenseFor(NAMESPACE, NAME, VERSION))
                .thenReturn(Optional.of(new LicenseService.LicenseInfo(LOCATION, List.of(LICENSE))));

        mockMvc.perform(post(PACKAGE_URL + "?force=yes", NAMESPACE, NAME, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));

        verify(service).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
    }
}
