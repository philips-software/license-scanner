package com.philips.research.licensescanner.core.domain.download;

import com.philips.research.licensescanner.core.command.ShellCommand;
import com.philips.research.licensescanner.core.command.ShellException;

import java.net.URI;
import java.nio.file.Path;

/**
 * GIT version control downloader.
 * Expects command line "git" to be installed.
 */
public class GitHandler implements VcsHandler {
    @Override
    public void download(Path directory, DownloadLocation location) {
        final var revision = location.getRevision().orElse("master");

        try {
            checkoutBranchOrTag(directory, location.getRepositoryUrl(), revision);
        } catch (ShellException e) {
            checkoutCommit(directory, location.getRepositoryUrl(), revision);
        }
    }

    private void checkoutBranchOrTag(Path directory, URI repository, String branchOrTag) {
        new ShellCommand("git")
                .setDirectory(directory.toFile())
                .execute("clone", "--depth=1", repository, "--branch", branchOrTag, directory);
    }

    private void checkoutCommit(Path directory, URI repository, String commitHash) {
        new ShellCommand("git")
                .setDirectory(directory.toFile())
                .execute("clone", repository, directory)
                .execute("checkout", commitHash);
    }
}
