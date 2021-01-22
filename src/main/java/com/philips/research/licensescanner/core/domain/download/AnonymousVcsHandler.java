/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.core.command.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tlinkowski.annotation.basic.NullOr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;

/**
 * Download handler for file and internet resources.
 */
public class AnonymousVcsHandler implements VcsHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AnonymousVcsHandler.class);
    private static final Duration MAX_EXTRACT_DURATION = Duration.ofMinutes(10);

    @Override
    public Path download(Path directory, URI location) {
        validateDirectory(directory);
        copyFile(target(directory, location), location);
        final var path = extractArchives(directory);
        final @NullOr String fragment = location.getFragment();

        return (fragment != null) ? path.resolve(fragment) : path;
    }

    private void validateDirectory(Path directory) {
        final var file = directory.toFile();
        if (!file.exists() || !file.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
    }

    private File target(Path directory, URI location) {
        final var filename = filenameFor(location);
        return directory.resolve(filename).toFile();
    }

    private void copyFile(File target, URI fromUri) {
        LOG.info("Download file from {} to {}", fromUri, target);
        try (FileOutputStream out = new FileOutputStream(target)) {
            final var url = fromUri.toURL();
            ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
            FileChannel outChannel = out.getChannel();
            outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
            inChannel.close();
        } catch (IOException e) {
            throw new DownloadException("File transfer failed", e);
        }
    }

    private String filenameFor(URI uri) {
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getSchemeSpecificPart()).getName();
        }

        final var path = uri.getPath();
        final var pos = path.lastIndexOf('/');
        return (pos >= 0) ? path.substring(pos + 1) : path;
    }

    private Path extractArchives(Path directory) {
        //noinspection SpellCheckingInspection
        final var baseDir = directory.toFile();
        new ShellCommand("extractcode").setDirectory(baseDir)
                .setTimeout(MAX_EXTRACT_DURATION)
                .execute("--verbose", "--shallow", "--replace-originals", ".");
        //noinspection ConstantConditions
        return Arrays.stream(baseDir.listFiles())
                .filter(File::isDirectory)
                .findFirst().orElse(baseDir)
                .toPath();
    }
}
