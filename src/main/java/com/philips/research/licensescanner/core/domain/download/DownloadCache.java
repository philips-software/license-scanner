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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import pl.tlinkowski.annotation.basic.NullOr;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cache for the source code of packages.
 * <p>
 * Avoids downloading the same VCS archive multiple times if various paths
 * of the same archive are accessed sequentially.
 */
@Component
public class DownloadCache {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadCache.class);

    private final Object lock = new Object();
    private final Downloader downloader;
    private final Path workDirectory;
    private final List<CacheEntry> cache = new ArrayList<>();
    private final int cacheSize;

    public DownloadCache(Downloader downloader, ApplicationConfiguration configuration) {
        this.downloader = downloader;
        this.cacheSize = configuration.getCacheSize();
        try {
            workDirectory = Files.createTempDirectory(configuration.getTempDir(), "licenses-");
            LOG.info("Cache directory is " + workDirectory);
        } catch (IOException e) {
            throw new DownloadException("Failed to create a working directory", e);
        }
    }

    /**
     * Obtains a cache entry with the package source files.
     *
     * @param location location to download the sources from
     * @return root directory of the package source files
     */
    public Path obtain(URI location) {
        synchronized (lock) {
            URI baseLocation = stripDirectoryPath(location);

            cleanup();
            final var entry = findOrCreateEntry(baseLocation);
            entry.claim();
            return entry.getRoot();
        }
    }

    private void cleanup() {
        final var dispose = cache.stream()
                .filter(entry -> !entry.isUsed())
                .limit(Math.max(0, cache.size() - cacheSize))
                .collect(Collectors.toList());
        for (var entry : dispose) {
            entry.dispose();
            cache.remove(entry);
        }
    }

    private CacheEntry findOrCreateEntry(URI location) {
        final var entry = cache.stream()
                .filter(e -> e.location.equals(location))
                .findFirst()
                .orElseGet(() -> new CacheEntry(location));
        cache.remove(entry);
        cache.add(entry);
        return entry;
    }

    /**
     * Releases the claim on the cache entry
     *
     * @param location VCS URL
     */
    public void release(URI location) {
        final var baseLocation = stripDirectoryPath(location);
        synchronized (lock) {
            cache.stream()
                    .filter(entry -> entry.location.equals(baseLocation))
                    .findFirst()
                    .ifPresent(CacheEntry::release);
        }
    }

    private URI stripDirectoryPath(URI location) {
        try {
            return new URI(location.getScheme(), location.getRawSchemeSpecificPart(), null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("location is not a valid URL");
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            LOG.info("Cleaning up the cache directory");
            FileSystemUtils.deleteRecursively(workDirectory);
        } catch (IOException e) {
            LOG.error("Failed to remove caches from {}", workDirectory);
        }
    }

    private class CacheEntry {
        private final URI location;
        private final Path store;
        private int usage = 0;
        private @NullOr Path root;

        CacheEntry(URI location) {
            LOG.info("Create cache for {}", location);
            this.location = location;
            store = workDirectory.resolve(UUID.randomUUID().toString());
            if (!store.toFile().mkdir()) {
                throw new IllegalStateException("Failed to create cache directory " + store);
            }
        }

        synchronized Path getRoot() {
            try {
                if (root == null) {
                    root = downloader.download(store, location);
                }
                return root;
            } catch (Exception e) {
                synchronized (lock) {
                    cache.remove(this);
                    dispose();
                }
                throw e;
            }
        }

        synchronized boolean isUsed() {
            return usage > 0;
        }

        synchronized void claim() {
            usage++;
            LOG.info("Claim #{} of cache for {}", usage, location);
        }

        synchronized void release() {
            LOG.info("Release #{} of cache for {}", usage, location);
            usage--;
        }

        void dispose() {
            try {
                LOG.info("Dispose cache for {}", location);
                FileSystemUtils.deleteRecursively(store);
            } catch (IOException e) {
                LOG.warn("Could not remove cache directory " + store);
            }
        }
    }
}
