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
public class GitVcsHandler implements VcsHandler {
    public static final Logger LOG = LoggerFactory.getLogger(GitVcsHandler.class);

    private final ShellCommand git = new ShellCommand("git").setTimeout(Duration.ofMinutes(5));

    @Override
    public Path download(Path directory, URI location) {
        git.setDirectory(directory.toFile());

        checkout(directory, repositoryFrom(location), versionFrom(location));

        return directory;
    }

    private String repositoryFrom(URI location) {
        final var raw = location.getRawSchemeSpecificPart();
        final var pos = raw.indexOf('@');
        final var path = (pos >= 0)
                ? URLDecoder.decode(raw.substring(0, pos), StandardCharsets.UTF_8)
                : location.getSchemeSpecificPart();
        return gitSchemeFrom(location) + path;
    }

    private String gitSchemeFrom(URI location) {
        final String plain = location.toString();
        if (location.getScheme() == null || plain.startsWith("ssh:git") || plain.startsWith("git@")) {
            return "";
        }
        return location.getScheme() + ':';
    }

    private String versionFrom(URI location) {
        final var raw = location.getRawSchemeSpecificPart();
        final var pos = raw.indexOf('@');
        return (pos >= 0) ? raw.substring(pos + 1) : "";
    }

    private void checkout(Path target, String repository, String version) {
        if (version.isBlank()) {
            checkoutDefaultBranch(target, repository);
        } else {
            checkoutVersion(target, repository, version);
        }
    }

    private void checkoutDefaultBranch(Path target, String repository) {
        try {
            LOG.info("Checkout default branch from {} to {}", repository, target);
            git.execute("clone", "--depth=1", repository, target);
        } catch (ShellException e) {
            throw new DownloadException("Checkout of default branch failed", e);
        }
    }

    private void checkoutVersion(Path target, String repository, String version) {
        try {
            checkoutBranchOrTag(target, repository, version, 'v' + version);
        } catch (DownloadException e) {
            LOG.info("Checkout by version branch or tag failed; attempting checkout by commit");
            checkoutCommit(target, repository, version);
        }
    }

    private void checkoutBranchOrTag(Path target, String repository, String... tags) {
        for (var tag : tags) {
            try {
                git.execute("clone", "--depth=1", repository, "--branch", tag, target);
                LOG.info("Checked out branch/tag '{}' from {} to {}", tag, repository, target);
                return;
            } catch (ShellException e) {
                LOG.info("Failed to checkout from branch or tag '{}'", tag);
            }
        }
        throw new DownloadException("Failed to checkout by branch/tag");
    }

    private void checkoutCommit(Path target, String repository, String commitHash) {
        try {
            LOG.info("Checking out commit '{}' from {} to {}", commitHash, repository, target);
            git.execute("clone", repository, target).execute("checkout", commitHash);
        } catch (ShellException e) {
            throw new DownloadException("Checkout by commit failed", e);
        }
    }
}
