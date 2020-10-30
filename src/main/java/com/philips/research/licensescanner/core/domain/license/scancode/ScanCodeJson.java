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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.philips.research.licensescanner.core.domain.Scan;
import com.philips.research.licensescanner.core.domain.license.License;
import com.philips.research.licensescanner.core.domain.license.LicenseParser;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScanCode Toolkit JSON result file mapping.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class ScanCodeJson {
    @JsonProperty("files")
    final List<FileJson> files = new ArrayList<>();

    /**
     * Adds all (mapped) license expressions from each file to the scan.
     */
    public void addScanResultsTo(Scan scan) {
        files.forEach(f -> f.addLicenseExpressionsTo(scan));
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class FileJson {
    @JsonProperty("licenses")
    final List<LicenseJson> licenses = new ArrayList<>();
    @JsonProperty("license_expressions")
    final List<String> expressions = new ArrayList<>();
    private final Map<String, LicenseJson> licenseDictionary = new HashMap<>();
    @JsonProperty("path")
    @NullOr
    String path = "";

    void addLicenseExpressionsTo(Scan scan) {
        buildDictionary();

        expressions.forEach(exp -> {
            final var scanner = new ExpressionScanner();
            scanner.scan(exp);

            var file = new File(path != null ? path : ".");
            scan.addDetection(scanner.license, scanner.score, file, scanner.startLine, scanner.endLine);
        });
    }

    private void buildDictionary() {
        for (var license : licenses) {
            final var existing = licenseDictionary.get(license.key);
            if (license.key != null && (existing == null || license.score > existing.score
                    || (license.score == existing.score && license.lines() > existing.lines()))) {
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
        private License license = License.NONE;

        void scan(String expression) {
            license = LicenseParser.parse(toSpdx(expression));
        }

        private String toSpdx(String expression) {
            final var converted = new StringBuilder();
            var key = new StringBuilder();
            for (var ch : expression.toCharArray()) {
                switch (ch) {
                    case ' ':
                    case '(':
                    case ')':
                        final var spdx = handleKey(key.toString());
                        converted.append(spdx);
                        converted.append(ch);
                        key.setLength(0);
                        break;
                    default:
                        key.append(ch);
                        break;
                }
            }
            converted.append(handleKey(key.toString()));

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

@JsonIgnoreProperties(ignoreUnknown = true)
class LicenseJson {
    @JsonProperty("key")
    @NullOr
    String key;
    @JsonProperty("score")
    double score;
    @JsonProperty("start_line")
    int startLine;
    @JsonProperty("end_line")
    int endLine;
    @JsonProperty("spdx_license_key")
    @NullOr String spdx;
    @JsonProperty("matched_rule")
    @NullOr MatchedRule matchedRule;

    String getSpdxIdentifier() {
        return (spdx != null)
                ? spdx
                : (key != null) ? key : "";
    }

    public int lines() {
        return 1 + endLine - startLine;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class MatchedRule {
    @JsonProperty("is_license_text")
    boolean isLicenseText;
}

