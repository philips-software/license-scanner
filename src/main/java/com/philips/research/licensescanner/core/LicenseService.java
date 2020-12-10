/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    List<URI> findPackages(String namespace, String name, String version);

    /**
     * @return License information if the package is known.
     */
    Optional<LicenseDto> licenseFor(URI packageUrl);

    /**
     * Queues package for scanning.
     *
     * @param vcsId Version control coordinates
     */
    void scanLicense(URI packageUrl, @NullOr URI vcsId);

    /**
     * @return the details for the indicated scan
     */
    Optional<LicenseDto> getScan(UUID scanId);

    /**
     * Lists all latest scan results for the indicated period.
     *
     * @return scan results
     */
    List<LicenseDto> findScans(Instant from, Instant until);

    /**
     * @return List of scanning errors
     */
    List<LicenseDto> findErrors();

    /**
     * @return List of contested licenses
     */
    List<LicenseDto> findContested();

    /**
     * Contests a scan
     *
     * @param scanId  contested scan
     * @param license suggested replacement
     */
    void contest(UUID scanId, @NullOr String license);

    /**
     * Confirms or corrects the license of a scan.
     *
     * @param license Updated value or null to confirm existing
     */
    void curateLicense(UUID scanId, @NullOr String license);

    /**
     * Clears all data for the indicated package.
     */
    void deletePackage(URI packageUrl);

    /**
     * Raise false-positive detection
     *
     * @param license detection to ignore
     */
    void ignore(UUID scanId, String license);

    /**
     * Restore false-positive detection
     *
     * @param license detection to restore
     */
    void restore(UUID scanId, String license);

    /**
     * @param scanId  identifier of the scan containing the detection
     * @param license identifier for the detecction
     * @param margin  number of lines around the proof fragment
     * @return file fragment for the detected license
     */
    Optional<FileFragmentDto> sourceFragment(UUID scanId, String license, int margin);

    /**
     * @return scanning statistics
     */
    StatisticsDto statistics();

    /**
     * Response model for license information.
     */
    @SuppressWarnings("NotNullFieldNotInitialized")
    class LicenseDto {
        public UUID uuid;
        public Instant timestamp;
        public URI purl;
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

    class FileFragmentDto {
        public String filename;
        public int firstLine;
        public int focusStart;
        public int focusEnd;
        public List<String> lines;
    }
}
