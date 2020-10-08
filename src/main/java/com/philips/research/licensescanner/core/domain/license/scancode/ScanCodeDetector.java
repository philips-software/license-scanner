/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain.license.scancode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.licensescanner.core.command.ShellCommand;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.Detector;
import com.philips.research.licensescanner.core.domain.license.DetectorException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Spring component for detecting licenses using the ScanCode Toolkit detector.
 *
 * @see <a href="https://github.com/nexB/scancode-toolkit">ScanCode Toolkit</a>
 */
@Component
public class ScanCodeDetector implements Detector {
    private static final String RESULT_FILE = "scancode.json";
    private static final Duration MAX_EXTRACT_DURATION = Duration.ofMinutes(10);
    private static final Duration MAX_SCAN_DURATION = Duration.ofMinutes(30);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void scan(Path directory, Scan scan, int scoreThreshold) {
        //noinspection SpellCheckingInspection
        new ShellCommand("extractcode").setDirectory(directory.toFile())
                .setTimeout(MAX_EXTRACT_DURATION)
                .execute("--verbose", ".");
        new ShellCommand("scancode")
                .setDirectory(directory.toFile())
                .setTimeout(MAX_SCAN_DURATION)
                .execute("--license", "-n2", "--verbose", "--timeout=" + MAX_SCAN_DURATION.toSeconds(), "--only-findings",
                        "--license-score", scoreThreshold, "--strip-root", "--ignore", "test*", "--ignore", RESULT_FILE,
                        "--json-pp", RESULT_FILE, ".");
        parseResult(directory, scan);
    }

    private void parseResult(Path directory, Scan scan) {
        try {
            final var scanResult = MAPPER.readValue(directory.resolve(RESULT_FILE).toFile(), ScanCodeJson.class);
            scanResult.addScanResultsTo(scan);
        } catch (IOException e) {
            throw new DetectorException("Failed to read ScanCode result file", e);
        }
    }
}
