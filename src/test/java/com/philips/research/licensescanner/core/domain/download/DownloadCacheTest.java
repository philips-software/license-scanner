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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DownloadCacheTest {
    private static final int CACHE_SIZE = 2;
    private static final URI BASE_LOCATION = URI.create("https://www.example.com/download");
    private static final URI LOCATION = BASE_LOCATION.resolve("#directory/path");
    private static final String DOWNLOAD = "download";
    @SuppressWarnings("NotNullFieldNotInitialized")
    private static Path TEMP_DIR;
    private final Downloader downloader = mock(Downloader.class);
    private final ApplicationConfiguration configuration = new ApplicationConfiguration()
            .setTempDir(TEMP_DIR).setCacheSize(CACHE_SIZE);
    private final DownloadCache cache = new DownloadCache(downloader, configuration);

    @BeforeAll
    static void beforeAll() throws Exception {
        TEMP_DIR = Files.createTempDirectory("Test-");
    }

    @AfterAll
    static void afterAll() throws Exception {
        FileSystemUtils.deleteRecursively(TEMP_DIR);
    }

    @BeforeEach
    void beforeEach() {
        when(downloader.download(any(Path.class), eq(BASE_LOCATION))).thenAnswer((answer) -> {
            final var path = (Path) answer.getArgument(0);
            final var download = path.resolve(DOWNLOAD);
            assertThat(download.toFile().mkdir()).isTrue();
            return download;
        });
    }

    @Test
    void cachesDownloadedPackageWorkDirectory() {
        final var workDir = cache.obtain(LOCATION);
        cache.release(LOCATION);

        assertThat(workDir.toFile()).exists();
        verify(downloader).download(workDir.getParent(), BASE_LOCATION);
    }

    @Test
    void reusesCache() {
        final var first = cache.obtain(LOCATION);
        cache.release(LOCATION);

        final var second = cache.obtain(LOCATION);

        assertThat(second).isEqualTo(first);
        verify(downloader, times(1)).download(any(Path.class), any(URI.class));
    }

    @Test
    void releasesCache() {
        final var workDir = cache.obtain(LOCATION);
        cache.release(LOCATION);
        for (int i = 0; i < CACHE_SIZE + 1; i++) {
            final var location = URI.create("https://example.com/" + i);
            cache.obtain(location);
            cache.release(location);
        }

        assertThat(workDir.toFile()).doesNotExist();
    }

    @Test
    void dropsEntry_downloadException() {
        when(downloader.download(any(Path.class), any(URI.class))).thenThrow(new IllegalStateException("Download issue"));

        assertThatThrownBy(() -> cache.obtain(LOCATION));
        assertThatThrownBy(() -> cache.obtain(LOCATION));

        verify(downloader, times(2)).download(any(), any());
    }
}
