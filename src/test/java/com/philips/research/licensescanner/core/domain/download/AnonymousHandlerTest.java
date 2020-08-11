package com.philips.research.licensescanner.core.domain.download;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnonymousHandlerTest extends DownloadHandlerTestBase {
    private static final String FILE = "dummy.txt";
    private static final Path PATH = Path.of("src", "test", "resources");
    private final DownloadHandler handler = new AnonymousHandler();

    @Test
    void downloadsFromFileURI() {
        handler.download(tempDir, PATH.resolve(FILE).toUri());

        assertThat(tempDir.resolve(FILE).toFile().exists()).isTrue();
    }

    @Test
    void throws_nonExistingFile() {
        assertThatThrownBy(() -> handler.download(tempDir, Path.of("not_a_file").toUri()))
                .isInstanceOf(DownloadException.class)
                .hasMessageContaining("File transfer");
    }

    @Test
    void downloadsFromWebURL() {
        handler.download(tempDir, URI.create("https://example.com/index.html"));

        assertThat(tempDir.resolve("index.html").toFile().exists()).isTrue();
    }
}
