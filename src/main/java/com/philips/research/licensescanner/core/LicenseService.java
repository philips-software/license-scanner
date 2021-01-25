/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * License related use-cases.
 */
public interface LicenseService {
    /**
     * Finds all packages matching the provided mask.
     *
     * @param namespace (optional) namespace mask
     * @param name      (optional) name mask
     * @param version   (optional) version mask
     * @return list of matching package URLs
     */
    List<ScanDto> findScans(String namespace, String name, String version);

    /**
     * @return License information if the package is known.
     */
    Optional<ScanDto> scanFor(URI purl);

    /**
     * Queues package for scanning.
     *
     * @param vcsId Version control coordinates
     */
    void scanLicense(URI packageUrl, @NullOr URI vcsId);

    /**
     * @return the details for the indicated scan
     */
    Optional<ScanDto> getScan(URI purl);

    /**
     * Lists all latest scan results for the indicated period.
     *
     * @return scan results
     */
    List<ScanDto> findScans(Instant from, Instant until);

    /**
     * @return List of scanning errors
     */
    List<ScanDto> findErrors();

    /**
     * @return List of contested licenses
     */
    List<ScanDto> findContested();

    /**
     * Contests a scan
     *
     * @param purl    contested scan
     * @param license suggested replacement
     */
    void contest(URI purl, @NullOr String license);

    /**
     * Confirms or corrects the license of a scan.
     *
     * @param license Updated value or null to confirm existing
     */
    void curateLicense(URI purl, @NullOr String license);

    /**
     * Clears all data for the indicated package.
     */
    void deleteScan(URI purl);

    /**
     * Raise false-positive detection
     *
     * @param license detection to ignore
     */
    void ignore(URI purl, String license);

    /**
     * Restore false-positive detection
     *
     * @param license detection to restore
     */
    void restore(URI purl, String license);

    /**
     * @param purl    identifier of the scan containing the detection
     * @param license identifier for the detecction
     * @param margin  number of lines around the proof fragment
     * @return file fragment for the detected license
     */
    Optional<FileFragmentDto> sourceFragment(URI purl, String license, int margin);

    /**
     * @return scanning statistics
     */
    StatisticsDto statistics();

    /**
     * Response model for license information.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class ScanDto {
        public URI purl;
        public Instant timestamp;
        public String license;
        public @NullOr URI location;
        public @NullOr String error;
        public @NullOr List<DetectionDto> detections;
        public @NullOr String contesting;
        public boolean isConfirmed;
    }

    /**
     * Response model for license detection information.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class DetectionDto {
        public String license;
        public String file;
        public int startLine;
        public int endLine;
        public int confirmations;
        public boolean ignored;
    }

    /**
     * Response model for statistics information.
     */
    class StatisticsDto {
        public int errors;
        public int contested;
        public int licenses;
    }

    /**
     * Response model for an annotated source file.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class FileFragmentDto {
        public String filename;
        public int firstLine;
        public int focusStart;
        public int focusEnd;
        public List<String> lines;
    }
}
