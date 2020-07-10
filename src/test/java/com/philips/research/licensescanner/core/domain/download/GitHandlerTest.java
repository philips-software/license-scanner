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
        handler.download(tempDir, VcsUri.from(URI.create("git+https://github.com/excalith/git-cheats.git@v1.0.0")));

        assertHash("61b307521005e98474243b8546a62a56e8e561b2");
    }

    @Test
    void checksOutByCommit() throws IOException {
        handler.download(tempDir, VcsUri.from(URI.create("git+https://github.com/excalith/git-cheats.git@4c9d714")));

        assertHash("4c9d7140896202504e76c79e499177d7f5414755");
    }
}
