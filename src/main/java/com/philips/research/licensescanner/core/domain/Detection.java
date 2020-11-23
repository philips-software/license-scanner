/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.license.License;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * Evidence reference where a license was detected.
 * Keeps track of the location with the highest percentage match, and the number of matches found.
 */
public class Detection {
    private static final File NO_FILE = new File("?");
    private static final String[] UNLIKELY = {"test", "sample", "docs", "demo", "tutorial", "changelog"};

    private final License license;

    private int score;
    private int confirmations;
    private File filePath = NO_FILE;
    private int startLine;
    private int endLine;
    private boolean ignored = true;

    public Detection(License license) {
        this.license = license;
    }

    /**
     * Adds new evidence to the detected license.
     *
     * @param score     percentage certainty about the license
     * @param filePath  file containing the evidence
     * @param startLine starting line in the file
     * @param endLine   ending line in the file
     */
    public void addEvidence(int score, File filePath, int startLine, int endLine) {
        final var suspicious = isSuspicious(filePath);
        if ((ignored && !suspicious) || score > this.score ||
                (score == this.score && (endLine - startLine > this.endLine - this.startLine))) {
            this.score = score;
            this.filePath = filePath;
            this.startLine = startLine;
            this.endLine = endLine;
            this.ignored &= suspicious;
        }
        confirmations++;
    }

    private boolean isSuspicious(File file) {
        final var lowercase = file.toString().toLowerCase();
        return Arrays.stream(UNLIKELY).anyMatch(lowercase::contains);
    }

    @SuppressWarnings("JpaAttributeTypeInspection")
    public License getLicense() {
        return license;
    }

    public int getScore() {
        return score;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public File getFilePath() {
        return filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getLineCount() {
        return endLine - startLine + 1;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Detection)) return false;
        Detection detection = (Detection) o;
        return Objects.equals(license, detection.license);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(license);
    }

}
