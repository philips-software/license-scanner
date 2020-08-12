package com.philips.research.licensescanner.core.domain.license.scancode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.licensescanner.core.command.ShellCommand;
import com.philips.research.licensescanner.core.domain.license.Copyright;
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
    private static final Duration MAX_DURATION = Duration.ofMinutes(10);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Copyright scan(Path directory) {

        new ShellCommand("extractcode").setDirectory(directory.toFile())
                .execute("--verbose", ".");
        new ShellCommand("scancode")
                .setDirectory(directory.toFile())
                .setTimeout(MAX_DURATION)
                .execute("--license", "-n2", "--verbose", "--timeout=" + MAX_DURATION.toSeconds(), "--only-findings",
                        "--strip-root", "--ignore", "test*", "--ignore", RESULT_FILE, "--json-pp", RESULT_FILE, ".");
        try {
            final var scanResult = MAPPER.readValue(directory.resolve(RESULT_FILE).toFile(), ScanCodeJson.class);

            final var copyright = new Copyright();
            scanResult.getLicenses().forEach(copyright::addLicense);

            return copyright;
        } catch (IOException e) {
            throw new DetectorException("Failed to read ScanCode result file", e);
        }
    }
}
