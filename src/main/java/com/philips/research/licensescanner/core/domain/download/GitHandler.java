/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.core.command.ShellCommand;
import com.philips.research.licensescanner.core.command.ShellException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

/**
 * GIT version control downloader.
 * Expects command line "git" to be installed.
 */
public class GitHandler implements DownloadHandler {
    public static final Logger LOG = LoggerFactory.getLogger(GitHandler.class);

    private final ShellCommand git = new ShellCommand("git").setTimeout(Duration.ofMinutes(5));

    @Override
    public void download(Path directory, URI location) {
        var path = location.getSchemeSpecificPart();
        var revision = "";
        final var pos = path.indexOf('@');
        if (pos >= 0) {
            revision = path.substring(pos + 1);
            path = path.substring(0, pos);
        }
        final var uri = URI.create(location.getScheme() + ':' + path);

        git.setDirectory(directory.toFile());

        checkout(directory, uri, revision);
    }

    private void checkout(Path target, URI repository, String revision) {
        try {
            checkoutBranchOrTag(target, repository, revision);
        } catch (DownloadException e) {
            LOG.info("Checkout by branch/tag failed; attempting checkout by commit");
            checkoutCommit(target, repository, revision);
        }
    }

    private void checkoutBranchOrTag(Path target, URI repository, String branchOrTag) {
        try {
            LOG.info("Checkout branch/tag '{}' from {} to {}", branchOrTag, repository, target);
            git.execute("clone", "--depth=1", repository, "--branch", branchOrTag, target);
        } catch (ShellException e) {
            throw new DownloadException("Checkout by branch/tag failed", e);
        }
    }

    private void checkoutCommit(Path target, URI repository, String commitHash) {
        try {
            LOG.info("Checkout commit '{}' from {} to {}", commitHash, repository, target);
            git.execute("clone", repository, target).execute("checkout", commitHash);
        } catch (ShellException e) {
            throw new DownloadException("Checkout by commit failed", e);
        }
    }
}
