/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.download;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnonymousVcsHandlerTest extends VcsHandlerTestBase {
    private static final Path RESOURCES_PATH = Path.of("src", "test", "resources");
    private static final String SAMPLE_ZIP = "sample.zip";
    private static final String SAMPLE_FILE = "sample.txt";
    private final VcsHandler handler = new AnonymousVcsHandler();

    @Test
    void throws_targetDirectoryDoesNotExist() {
        assertThatThrownBy(() -> handler.download(Path.of("DoesNotExist"), RESOURCES_PATH.toUri()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a directory");
    }

    @Test
    void downloadsFromFileURI() {
        final var dir = handler.download(tempDir, RESOURCES_PATH.resolve(SAMPLE_FILE).toUri());

        assertThat(tempDir.resolve(SAMPLE_FILE).toFile()).exists();
        assertThat(dir).isEqualTo(tempDir);
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

        assertThat(tempDir.resolve("index.html").toFile()).exists();
    }

    @Test
    void extractsArchivesAfterDownload() {
        final var dir = handler.download(tempDir, RESOURCES_PATH.resolve(SAMPLE_ZIP).toUri());

        assertThat(tempDir.resolve(SAMPLE_ZIP).resolve("sample").resolve("sample.txt").toFile()).exists();
        assertThat(dir).isEqualTo(tempDir.resolve(SAMPLE_ZIP));
    }

    @Test
    void indicatesPathFromLocation() {
        final var dir = handler.download(tempDir, RESOURCES_PATH.toUri().resolve("#sample%2Fpath"));

        assertThat(dir).isEqualTo(tempDir.resolve("sample").resolve("path"));
    }
}
