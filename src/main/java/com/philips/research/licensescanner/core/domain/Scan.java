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
    private final Instant timestamp = Instant.now();
    private final URI purl;
    private final @NullOr URI location;
    private final Map<License, Detection> detections = new HashMap<>();

    @SuppressWarnings("JpaAttributeTypeInspection")
    private @NullOr License license;
    private @NullOr String error;
    private @NullOr License contesting;

    public Scan(URI purl, @NullOr URI location) {
        this.purl = purl;
        this.location = location;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public URI getPurl() {
        return purl;
    }

    public License getLicense() {
        if (license != null) {
            return license;
        }
        if (detections.isEmpty()) {
            return License.NONE;
        }
        return detections.entrySet().stream()
                .filter(entry -> !entry.getValue().isIgnored())
                .map(Map.Entry::getKey)
                .reduce(License.NONE, License::and);
    }

    /**
     * Raises question about the validity of an unconfirmed the license.
     * If the license was already confirmed, nothing happens.
     */
    public Scan contest(@NullOr License license) {
        if (license == null || license.equals(getLicense())) {
            contesting = null;
        } else if (!isOverride() && (error == null) && isImprovingContestingLicense(license)) {
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
        contesting = null;
        error = null;
        return this;
    }

    public boolean isOverride() {
        return license != null;
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

    public void ignore(License license, boolean ignore) {
        getDetection(license).ifPresent(detection -> detection.setIgnored(ignore));
        contest(contesting);
    }

    public Optional<Detection> getDetection(License license) {
        return Optional.ofNullable(detections.get(license));
    }

    public Scan addDetection(License license, int score, File file, int startLine, int endLine) {
        var detection = detections.get(license);
        if (detection == null) {
            detection = newDetection(license);
            detections.put(license, detection);
        }
        detection.addEvidence(score, file, startLine, endLine);
        contest(contesting);

        return this;
    }

    /**
     * Ugly hack to allow creation of a persistent version.
     */
    protected Detection newDetection(License license) {
        return new Detection(license);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Scan)) return false;
        Scan scan = (Scan) o;
        return getPurl().equals(scan.getPurl());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getPurl());
    }

    @Override
    public String toString() {
        return purl.toString();
    }
}
