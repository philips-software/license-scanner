package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.license.License;

import java.io.File;
import java.util.Objects;

/**
 * Evidence reference where a license was detected.
 * Keeps track of the location with the highest percentage match, and the number of matches found.
 */
public class Detection {
    private static final File NO_FILE = new File("?");

    private final License license;

    private int score;
    private int confirmations;
    private File filePath = NO_FILE;
    private int startLine;
    private int endLine;

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
        if (score > this.score) {
            this.score = score;
            this.filePath = filePath;
            this.startLine = startLine;
            this.endLine = endLine;
        }
        confirmations++;
    }

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
