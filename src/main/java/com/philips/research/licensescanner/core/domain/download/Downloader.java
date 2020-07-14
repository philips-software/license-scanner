package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
interface VcsHandler {
    void download(Path directory, URI location);
}

@Component
public class Downloader {
    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    private final Path baseDirectory;
    private final Map<String, VcsHandler> registry = new HashMap<>();

    @Autowired
    public Downloader(ApplicationConfiguration configuration) {
        this.baseDirectory = configuration.getTempDir();

        register("", new AnonymousHandler());
        register("git", new GitHandler());
    }

    void register(String vcs, VcsHandler handler) {
        registry.put(vcs, handler);
    }

    public Path download(URI location) {
        final var handler = validHandler(location.getScheme());
        final var directory = newDirectory();
        final var uri = downloadUri(location);

        handler.download(directory, uri);

        return directory;
    }

    private VcsHandler validHandler(String scheme) {
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

        return URI.create(scheme + ":" + location.getSchemeSpecificPart());
    }
}
