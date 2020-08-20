package com.philips.research.licensescanner.core.domain.license.scancode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScanCode Toolkit JSON result file mapping.
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class ScanCodeJson {
    @JsonProperty("files")
    List<FileJson> files;

    /**
     * Adds all (mapped) license expressions from each file to the scan.
     */
    public void addScanResultsTo(Scan scan) {
        files.forEach(f -> f.addLicenseExpressionsTo(scan));
    }
}

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class FileJson {
    @JsonProperty("path")
    String path = "";
    @JsonProperty("licenses")
    List<LicenseJson> licenses;
    @JsonProperty("license_expressions")
    List<String> expressions;

    private final Map<String, LicenseJson> licenseDictionary = new HashMap<>();

    void addLicenseExpressionsTo(Scan scan) {
        buildDictionary();

        expressions.forEach(exp -> {
            final var scanner = new ExpressionScanner();
            scanner.scan(exp);

            scan.addDetection(scanner.license, scanner.score, new File(path), scanner.startLine, scanner.endLine);
        });
    }

    private void buildDictionary() {
        for (var license : licenses) {
            final var existing = licenseDictionary.get(license.key);
            if (existing == null || license.score > existing.score) {
                licenseDictionary.put(license.key, license);
            }
        }
    }

    /**
     * Extracts and collects license details from a key-based expression.
     */
    private class ExpressionScanner {
        private int startLine = Integer.MAX_VALUE;
        private int endLine = 0;
        private int score = 100;
        private License license;

        void scan(String expression) {
            license = LicenseParser.parse(toSpdx(expression));
        }

        private String toSpdx(String expression) {
            final var converted = new StringBuilder();
            var key = "";
            for (var ch : expression.toCharArray()) {
                switch (ch) {
                    case ' ':
                    case '(':
                    case ')':
                        final var spdx = handleKey(key);
                        converted.append(spdx);
                        converted.append(ch);
                        key = "";
                        break;
                    default:
                        key += ch;
                        break;
                }
            }
            converted.append(handleKey(key));

            return converted.toString();
        }

        String handleKey(String key) {
            final var lic = licenseDictionary.get(key);
            if (lic == null) {
                return key;
            }

            startLine = Math.min(startLine, lic.startLine);
            endLine = Math.max(endLine, lic.endLine);
            score = Math.min(score, (int) lic.score);
            return lic.getSpdxIdentifier();
        }
    }
}

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class LicenseJson {
    @JsonProperty("key")
    String key;
    @JsonProperty("score")
    double score;
    @JsonProperty("start_line")
    int startLine;
    @JsonProperty("end_line")
    int endLine;
    @JsonProperty("spdx_license_key")
    String spdx;

    String getSpdxIdentifier() {
        return (spdx != null) ? spdx : key;
    }
}

