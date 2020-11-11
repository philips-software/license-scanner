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

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnonymousHandlerTest extends DownloadHandlerTestBase {
    private static final Path PATH = Path.of("src", "test", "resources");
    private final DownloadHandler handler = new AnonymousHandler();

    @Test
    void downloadsFromFileURI() {
        handler.download(tempDir, PATH.resolve("dummy.txt").toUri());

        assertThat(tempDir.resolve("dummy.txt").toFile().exists()).isTrue();
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
        handler.download(tempDir, PATH.resolve("sample.zip").toUri());

        assertThat(tempDir.resolve("sample.zip-extract").resolve("sample").resolve("sample.txt").toFile()).exists();
    }
}
