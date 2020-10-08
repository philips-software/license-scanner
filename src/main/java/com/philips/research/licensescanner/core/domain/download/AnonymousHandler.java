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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

/**
 *
 */
public class AnonymousHandler implements DownloadHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AnonymousHandler.class);

    @Override
    public void download(Path directory, URI location) {
        copyFile(target(directory, location), location);
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
            return new File(uri).getName();
        }

        final var path = uri.getPath();
        final var pos = path.lastIndexOf('/');
        return (pos >= 0) ? path.substring(pos + 1) : path;
    }
}
