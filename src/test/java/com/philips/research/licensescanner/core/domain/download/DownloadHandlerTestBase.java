/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.download;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadHandlerTestBase {
    protected Path tempDir;

    @BeforeEach
    void beforeEach() throws IOException {
        tempDir = Files.createTempDirectory("test");
    }

    @AfterEach
    void afterEach() throws IOException {
        FileSystemUtils.deleteRecursively(tempDir);
    }
}
