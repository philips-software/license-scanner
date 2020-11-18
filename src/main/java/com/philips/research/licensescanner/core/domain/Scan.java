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
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.util.*;

/**
 * Result of a license scan for a package.
 */
public class Scan {
    private final UUID uuid = UUID.randomUUID();
    private final Instant timestamp = Instant.now();
    private final Package pkg;
    private final @NullOr URI location;
    private final Map<License, Detection> detections = new HashMap<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    private License license = License.NONE;
    private @NullOr String error;
    private @NullOr License contesting;
    private boolean confirmed;

    public Scan(Package pkg, @NullOr URI location) {
        this.pkg = pkg;
        this.location = location;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Package getPackage() {
        return pkg;
    }

    public License getLicense() {
        return license;
    }

    /**
     * Raises question about the validity of an unconfirmed the license.
     * If the license was already confirmed, nothing happens.
     */
    public Scan contest(License license) {
        if (!confirmed && (error == null) && isImprovingContestingLicense(license)) {
            contesting = license;
        }
        return this;
    }

    private boolean isImprovingContestingLicense(License license) {
        return !license.equals(License.NONE) || contesting == null || contesting.equals(License.NONE);
    }

    public Optional<License> getContesting() {
        return Optional.ofNullable(contesting);
    }

    public Scan confirm(License license) {
        this.license = license;
        confirmed = true;
        contesting = null;
        error = null;
        return this;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Optional<URI> getLocation() {
        return Optional.ofNullable(location);
    }

    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }

    public Scan setError(String message) {
        error = message;
        return this;
    }

    public List<Detection> getDetections() {
        return new ArrayList<>(detections.values());
    }

    public Optional<Detection> getDetection(License license) {
        return Optional.ofNullable(detections.get(license));
    }

    public Scan addDetection(License license, int score, File file, int startLine, int endLine) {
        var detection = detections.get(license);
        if (detection == null) {
            detection = newDetection(license);
            detections.put(license, detection);
            this.license = detections.keySet().stream()
                    .reduce(license, License::and);
        }
        detection.addEvidence(score, file, startLine, endLine);

        return this;
    }

    /**
     * Ugly hack to allow creation of a persistent version.
     */
    protected Detection newDetection(License license) {
        return new Detection(license);
    }

}
