package com.philips.research.licensescanner.core.domain.download;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GitHandlerTest {
    final VcsHandler handler = new GitHandler();

    private Path tempDir;

    @BeforeEach
    void beforeEach() throws IOException {
        tempDir = Files.createTempDirectory("test");
    }

    @AfterEach
    void afterEach() throws IOException {
        FileSystemUtils.deleteRecursively(tempDir);
    }

    private void assertHash(String s) throws IOException {
        assertThat(Files.readString(tempDir.resolve(".git").resolve("HEAD")))
                .isEqualTo(s + "\n");
    }

    @Test
    void checksOutByTag() throws IOException {
        handler.download(tempDir, VcsUri.from(URI.create("git+https://github.com/git/git.git@v2.15.0")));

        assertHash("cb5918aa0d50f50e83787f65c2ddc3dcb10159fe");
    }

    @Test
    void checksOutByCommit() throws IOException {
        handler.download(tempDir, VcsUri.from(URI.create("git+https://github.com/git/git.git@4a0fcf9f760c9774be77f51e1e88a7499b53d2e2")));

        assertHash("4a0fcf9f760c9774be77f51e1e88a7499b53d2e2");
    }
}
