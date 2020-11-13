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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitVcsHandlerTest extends VcsHandlerTestBase {
    final VcsHandler handler = new GitVcsHandler();

    private void assertHash(String s) throws IOException {
        assertThat(Files.readString(tempDir.resolve(".git").resolve("HEAD")))
                .isEqualTo(s + "\n");
    }

    @Test
    void checksOutRelease() throws IOException {
        final var path = handler.download(tempDir, URI.create("https://github.com/excalith%2Fgit-cheats.git@v1.0.0#some%2Fpath"));

        assertHash("61b307521005e98474243b8546a62a56e8e561b2");
        assertThat(path).isEqualTo(tempDir.resolve("some").resolve("path"));
    }

    @Test
    void checksOutUsingAlternativeVersionTags() throws IOException {
        final var path = handler.download(tempDir, URI.create("https://github.com/excalith%2Fgit-cheats.git@1.0.0"));

        assertHash("61b307521005e98474243b8546a62a56e8e561b2");
    }

    @Test
    void checksOutByCommitHash() throws IOException {
        handler.download(tempDir, URI.create("https://github.com/excalith/git-cheats.git@4c9d714"));

        assertHash("4c9d7140896202504e76c79e499177d7f5414755");
    }

    @Test
    @Disabled("Build server does not have an SSH key on GitHub")
    void supportsUserSpecificSshURI() throws IOException {
        handler.download(tempDir, URI.create("ssh:git%40github.com:excalith/git-cheats.git@v1.0.0"));

        assertHash("61b307521005e98474243b8546a62a56e8e561b2");
    }

    @Test
    void throws_gitFailures() {
        assertThatThrownBy(() -> handler.download(tempDir, URI.create("https://example.com/unknown")))
                .isInstanceOf(DownloadException.class)
                .hasMessageContaining("Checkout");
    }
}
