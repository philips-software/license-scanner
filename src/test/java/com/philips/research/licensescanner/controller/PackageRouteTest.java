package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String FILE = "path/to/file";
    private static final int START_LINE = 12;
    private static final int END_LINE = 23;
    private static final int CONFIRMATIONS = 42;
    private static final String BASE_URL = "/package";
    private static final String PACKAGE_URL = BASE_URL + "/{origin}/{pkg}/{version}";
    private static final String SCANS_URL = BASE_URL + "/scans";

    @MockBean
    LicenseService service;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        Mockito.reset(service);
    }

    private LicenseService.PackageId standardPackageId() {
        final var id = new LicenseService.PackageId();
        id.namespace = NAMESPACE;
        id.name = NAME;
        id.version = VERSION;
        return id;
    }

    private LicenseService.LicenseInfo standardLicenseInfo() {
        final var info = new LicenseService.LicenseInfo();
        info.pkg = standardPackageId();
        info.license = LICENSE;
        info.location = LOCATION;
        return info;
    }

    private LicenseService.LicenseInfo standardLicenseInfoWithDetection() {
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

    private JSONObject searchResult(JSONObject... objects) throws Exception {
        var array = new JSONArray();
        Arrays.stream(objects).forEach(array::put);
        return new JSONObject().put("results", array);
    }

    @Nested
    class FindPackages {
        @Test
        void findsPackageByAllFields() throws Exception {
            final var response = searchResult(new JSONObject().put("name", NAME));
            when(service.findPackages(NAMESPACE, NAME, VERSION))
                    .thenReturn(List.of(standardPackageId()));

            mockMvc.perform(get(BASE_URL + "?namespace={ns}&name={name}&version={version}", NAMESPACE, NAME, VERSION))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }

        @Test
        void findsPackagesByOptionalFields() throws Exception {
            final var response = searchResult(new JSONObject().put("name", NAME));
            when(service.findPackages("", "", ""))
                    .thenReturn(List.of(standardPackageId()));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }
    }

    @Nested
    class ScanResults {
        @Test
        void getsExistingScanResult() throws Exception {
            final var response = new JSONObject()
                    .put("license", LICENSE)
                    .put("detections", new JSONArray().put(new JSONObject().put("file", FILE)));
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.of(standardLicenseInfoWithDetection()));

            mockMvc.perform(get(PACKAGE_URL, NAMESPACE, NAME, VERSION))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));
        }

        @Test
        void getsExistingScanResultForEmptyNamespace() throws Exception {
            final var info = standardLicenseInfo();
            info.pkg.namespace = "";
            when(service.licenseFor(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.of(info));

            mockMvc.perform(get(PACKAGE_URL, "", NAME, VERSION))
                    .andExpect(status().isOk());

            verify(service).licenseFor("", NAME, VERSION);
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
            final var body = new JSONObject().put("location", "http://somewhere.com/else");
            final var response = new JSONObject().put("location", LOCATION);
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL, NAMESPACE, NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service, never()).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
        }

        @Test
        void scansPackagesWithoutNamespace() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);

            mockMvc.perform(post(PACKAGE_URL, "", NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk());

            verify(service).scanLicense("", NAME, VERSION, LOCATION);
        }

        @Test
        void schedulesNewScan_noScanExists() throws Exception {
            final var body = new JSONObject().put("location", LOCATION);
            final var response = new JSONObject().put("name", NAME);

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
            final var response = new JSONObject().put("name", NAME);
            when(service.licenseFor(NAMESPACE, NAME, VERSION))
                    .thenReturn(Optional.of(standardLicenseInfo()));

            mockMvc.perform(post(PACKAGE_URL + "?force=yes", NAMESPACE, NAME, VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(response.toString()));

            verify(service).scanLicense(NAMESPACE, NAME, VERSION, LOCATION);
        }

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
}
