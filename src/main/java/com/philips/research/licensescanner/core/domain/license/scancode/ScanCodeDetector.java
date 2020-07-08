package com.philips.research.licensescanner.core.domain.license.scancode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.licensescanner.core.command.ShellCommand;
import com.philips.research.licensescanner.core.domain.license.Copyright;
import com.philips.research.licensescanner.core.domain.license.Detector;
import com.philips.research.licensescanner.core.domain.license.DetectorException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ScanCodeDetector implements Detector {
    private static final String RESULT_FILE = "scancode.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Copyright scan(Path directory) {
        //TODO Should first extract any archives?
        new ShellCommand("scancode")
                .setDirectory(directory.toFile())
                .execute("-clp", "-n2", "--timeout=300", "--only-findings", "--strip-root", "--ignore", RESULT_FILE, "--json-pp", RESULT_FILE, ".");
        try {
            final var scanResult = MAPPER.readValue(directory.resolve(RESULT_FILE).toFile(), ScanCodeJson.class);
            final var licenses = scanResult.getLicense();

            return new Copyright(licenses);
        } catch (IOException e) {
            throw new DetectorException("Failed to read ScanCode result file", e);
        }
    }
}
