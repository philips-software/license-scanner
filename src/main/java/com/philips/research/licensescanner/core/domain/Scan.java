package com.philips.research.licensescanner.core.domain;

import com.philips.research.licensescanner.core.domain.license.License;

import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * Result of a license scan for a package.
 */
public class Scan {
    private final Package pkg;
    private final URI location;
    private final Map<License, Detection> detections = new HashMap<>();

    private License license = License.NONE;
    private String error;

    public Scan(Package pkg, URI location) {
        this.pkg = pkg;
        this.location = location;
    }

    public Package getPackage() {
        return pkg;
    }

    public License getLicense() {
        return license;
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
