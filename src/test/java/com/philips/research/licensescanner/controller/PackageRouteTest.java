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

import java.net.URI;
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
    private static final String ORIGIN = "origin";
    private static final String PACKAGE = "package";
    private static final String VERSION = "version";
    private static final URI VCS_URI = URI.create("git+ssh://example.com@1234");
    private static final String LICENSE = "MIT OR Apache-2.0";
    private static final String PACKAGE_URL = "/package/{origin}/{pkg}/{version}";
    @MockBean
    LicenseService service;
    @Autowired
    private MockMvc mockMvc;

    private JSONObject packageInfoJson() throws Exception {
        return new JSONObject()
                .put("origin", ORIGIN)
                .put("package", PACKAGE)
                .put("version", VERSION)
                .put("vcsUri", VCS_URI);
    }

    @Test
    void getsExistingScanResult() throws Exception {
        final var response = packageInfoJson().put("license", LICENSE);
        when(service.licenseFor(ORIGIN, PACKAGE, VERSION))
                .thenReturn(Optional.of(new LicenseService.LicenseInfo(LICENSE, VCS_URI)));

        mockMvc.perform(get(PACKAGE_URL, ORIGIN, PACKAGE, VERSION))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString(), true));
    }

    @Test
    void notFound_getUnknownPackage() throws Exception {
        when(service.licenseFor(ORIGIN, PACKAGE, VERSION))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(PACKAGE_URL, ORIGIN, PACKAGE, VERSION))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsEarlierScanResult_scannedBefore() throws Exception {
        final var body = new JSONObject().put("vcsUri", VCS_URI);
        final var response = packageInfoJson()
                .put("license", LICENSE);
        when(service.licenseFor(ORIGIN, PACKAGE, VERSION))
                .thenReturn(Optional.of(new LicenseService.LicenseInfo(LICENSE, VCS_URI)));

        mockMvc.perform(post(PACKAGE_URL, ORIGIN, PACKAGE, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString(), true));

        verify(service, never()).scanLicense(ORIGIN, PACKAGE, VERSION, VCS_URI);
    }

    @Test
    void schedulesNewScan_noScanExists() throws Exception {
        final var body = new JSONObject().put("vcsUri", VCS_URI);
        final var response = packageInfoJson();

        mockMvc.perform(post(PACKAGE_URL, ORIGIN, PACKAGE, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));

        verify(service).scanLicense(ORIGIN, PACKAGE, VERSION, VCS_URI);
    }

    @Test
    void forcesRescanning() throws Exception {
        final var body = new JSONObject().put("vcsUri", VCS_URI);
        final var response = packageInfoJson();
        when(service.licenseFor(ORIGIN, PACKAGE, VERSION))
                .thenReturn(Optional.of(new LicenseService.LicenseInfo(LICENSE, VCS_URI)));

        mockMvc.perform(post(PACKAGE_URL + "?force=yes", ORIGIN, PACKAGE, VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(response.toString()));

        verify(service).scanLicense(ORIGIN, PACKAGE, VERSION, VCS_URI);
    }
}
