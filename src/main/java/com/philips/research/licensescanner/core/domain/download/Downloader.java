/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Version control handler API.
 */
interface DownloadHandler {
    /**
     * Downloads package sources.
     *
     * @param directory target directory to store the sources
     * @param location  download location using the format {@code <transport>://<host_name>[/<path_to_repository>][@<revision_tag_or_branch>][#<sub_path>]}
     */
    void download(Path directory, URI location);
}

/**
 * Spring component delegating download of package source code from a provided URI to registered download handlers.
 */
@Component
public class Downloader {
    private final Path baseDirectory;
    private final Map<String, DownloadHandler> registry = new HashMap<>();

    @Autowired
    public Downloader(ApplicationConfiguration configuration) {
        this.baseDirectory = configuration.getTempDir();

        register("", new AnonymousHandler());
        register("git", new GitHandler());
    }

    /**
     * Registers a download handler for a single tool.
     *
     * @param tool tool identifier
     */
    void register(String tool, DownloadHandler handler) {
        registry.put(tool, handler);
    }

    /**
     * Downloads the source of a package from the provided location.
     *
     * @param location download location using the format {@code <vcs_tool>+<transport>://<host_name>[/<path_to_repository>][@<revision_tag_or_branch>][#<sub_path>]}
     * @return path to the downloaded sources
     * @throws DownloadException if downloading failed or no handler matches the location.
     */
    public Path download(URI location) {
        final var handler = validHandler(location.getScheme());
        final var directory = newDirectory();
        final var uri = downloadUri(location);

        try {
            handler.download(directory, uri);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            directory.toFile().delete();
        }

        return directory;
    }

    private DownloadHandler validHandler(String scheme) {
        final var pos = scheme.indexOf('+');
        final var tool = (pos >= 0) ? scheme.substring(0, pos) : "";

        final var handler = registry.get(tool);
        if (handler == null) {
            throw new DownloadException("No handler registered for '" + tool + "'");
        }
        return handler;
    }

    private Path newDirectory() {
        try {
            return Files.createTempDirectory(baseDirectory, "license-");
        } catch (IOException e) {
            throw new DownloadException("Failed to create a working directory", e);
        }
    }

    private URI downloadUri(URI location) {
        var scheme = location.getScheme();
        final var pos = location.getScheme().indexOf('+');
        if (pos >= 0) {
            scheme = scheme.substring(pos + 1);
        }

        return URI.create(scheme + ":" + location.getRawSchemeSpecificPart());
    }
}
