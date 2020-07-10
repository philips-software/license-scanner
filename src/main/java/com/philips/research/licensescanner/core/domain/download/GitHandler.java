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
public class GitHandler implements VcsHandler {
    public static final Logger LOG = LoggerFactory.getLogger(GitHandler.class);

    private final ShellCommand git = new ShellCommand("git").setTimeout(Duration.ofMinutes(5));

    @Override
    public void download(Path directory, VcsUri location) {
        final var revision = location.getRevision().orElse("master");
        git.setDirectory(directory.toFile());

        try {
            checkoutBranchOrTag(directory, location.getRepositoryUrl(), revision);
        } catch (DownloadException e) {
            LOG.info("Checkout by branch/tag failed; attempting checkout by commit");
            checkoutCommit(directory, location.getRepositoryUrl(), revision);
        }
    }

    private void checkoutBranchOrTag(Path directory, URI repository, String branchOrTag) {
        try {
            git.execute("clone", "--depth=1", repository, "--branch", branchOrTag, directory);
        } catch (ShellException e) {
            throw new DownloadException("Checkout by branch/tag failed", e);
        }
    }

    private void checkoutCommit(Path directory, URI repository, String commitHash) {
        try {
            git.execute("clone", repository, directory).execute("checkout", commitHash);
        } catch (ShellException e) {
            throw new DownloadException("Checkout by commit failed", e);
        }
    }
}
