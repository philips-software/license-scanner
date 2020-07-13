package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.ApplicationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Version control handler API.
 */
interface VcsHandler {
    void download(Path directory, VcsUri location);
}

@Component
public class Downloader {
    private final Path baseDirectory;

    private final Map<String, VcsHandler> registry = new HashMap<>();

    @Autowired
    public Downloader(ApplicationConfiguration configuration) {
        this.baseDirectory = configuration.getTempDir();

        register("git", new GitHandler());
    }

    void register(String vcs, VcsHandler handler) {
        registry.put(vcs, handler);
    }

    public Path download(VcsUri location) {
        final var handler = validHandler(location.getVcsTool());
        final var directory = newDirectory();

        handler.download(directory, location);

        return directory;
    }

    private VcsHandler validHandler(String tool) {
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
}
