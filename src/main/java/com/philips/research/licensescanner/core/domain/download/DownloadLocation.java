package com.philips.research.licensescanner.core.domain.download;

import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a download location specification in the format:
 * &lt;vcs_tool>+&lt;transport>://&lt;host_name>[/&lt;path_to_repository>][@&lt;revision_tag_or_branch>][#&lt;sub_path>]
 */
public class DownloadLocation {
    private final String vcsTool;
    private final URI repositoryUrl;
    private final String revision;
    private final File subPath;

    DownloadLocation(String vcsTool, URI repositoryUrl, String revision, File subPath) {
        this.vcsTool = vcsTool;
        this.repositoryUrl = repositoryUrl;
        this.revision = revision;
        this.subPath = subPath;
    }

    static public DownloadLocation parse(String specification) {
        final var pattern = Pattern.compile("^(\\w+)\\+([^\\s@#]+)(@([^\\s#]+))?(#(\\S+))?$");
        final var matcher = pattern.matcher(specification);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a valid download location: " + specification);
        }
        final var tool = matcher.group(1);
        final var url = matcher.group(2);
        final var version = matcher.group(4);
        final var path = matcher.group(6);

        return new DownloadLocation(tool, URI.create(url), version, (path != null) ? new File(path) : null);
    }

    public String getVcsTool() {
        return vcsTool;
    }

    public URI getRepositoryUrl() {
        return repositoryUrl;
    }

    public Optional<String> getRevision() {
        return Optional.ofNullable(revision);
    }

    public Optional<File> getSubPath() {
        return Optional.ofNullable(subPath);
    }

    @Override
    public String toString() {
        var location = vcsTool + "+" + repositoryUrl;
        if (revision != null) {
            location += "@" + revision;
        }
        if (subPath != null) {
            location += "#" + subPath;
        }
        return location;
    }
}
