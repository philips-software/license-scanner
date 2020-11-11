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

import com.philips.research.licensescanner.core.command.ShellCommand;
import com.philips.research.licensescanner.core.command.ShellException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
        var path = location.getRawSchemeSpecificPart();
        var version = "";
        final var pos = path.indexOf('@');
        if (pos >= 0) {
            version = path.substring(pos + 1);
            path = path.substring(0, pos);
        }
        final var uri = URI.create(location.getScheme() + ':' + URLDecoder.decode(path, StandardCharsets.UTF_8));

        git.setDirectory(directory.toFile());

        checkout(directory, uri, version);
    }

    private void checkout(Path target, URI repository, String version) {
        if (version.isBlank()) {
            checkoutMainBranch(target, repository);
        } else {
            checkoutBranchOrTag(target, repository, version);
        }
    }

    private void checkoutMainBranch(Path target, URI repository) {
        try {
            LOG.info("Checkout main branch from {} to {}", repository, target);
            git.execute("clone", "--depth=1", repository, target);
        } catch (ShellException e) {
            throw new DownloadException("Checkout of main branch failed", e);
        }
    }

    private void checkoutBranchOrTag(Path target, URI repository, String branchOrTag) {
        try {
            LOG.info("Checkout branch/tag '{}' from {} to {}", branchOrTag, repository, target);
            git.execute("clone", "--depth=1", repository, "--branch", branchOrTag, target);
        } catch (ShellException e) {
            LOG.info("Checkout by branch/tag failed; attempting checkout by commit instead");
            checkoutCommit(target, repository, branchOrTag);
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
