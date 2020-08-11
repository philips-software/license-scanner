package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.ApplicationConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DownloaderTest {
    private static final String TOOL = "tool";
    private static final URI LOCATION = URI.create("https://example.com@version");

    private static ApplicationConfiguration configuration;

    private final DownloadHandler mockHandler = mock(DownloadHandler.class);
    private final Downloader downloader = new Downloader(configuration);

    @BeforeAll()
    static void beforeAll() throws IOException {
        configuration = new ApplicationConfiguration();
        configuration.setTempDir(Files.createTempDirectory("test"));
    }

    @AfterAll
    static void afterAll() throws IOException {
        FileSystemUtils.deleteRecursively(configuration.getTempDir());
    }

    @BeforeEach
    void beforeEach() {
        downloader.register(TOOL, mockHandler);
    }

    @Test
    void throws_downloadForUnknownVcsTool() {
        final var unknown = URI.create("unknown+http://unknown.org");

        assertThatThrownBy(() -> downloader.download(unknown))
                .isInstanceOf(DownloadException.class)
                .hasMessageContaining("No handler registered");
    }

    @Test
    void downloadsForToolFromLocationToDirectory() {
        final var location = URI.create(TOOL + "+" + LOCATION.toString() + "#path/to/whatever");
        final var directory = downloader.download(location);

        assertThat(directory.getParent()).isEqualTo(configuration.getTempDir());
        verify(mockHandler).download(any(Path.class), eq(LOCATION));
    }
}
