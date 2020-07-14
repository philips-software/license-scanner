package com.philips.research.licensescanner.core.domain.download;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a version control location specification in the format:
 * &lt;vcs_tool>+&lt;transport>://&lt;host_name>[/&lt;path_to_repository>][@&lt;revision_tag_or_branch>][#&lt;sub_path>]
 */
public final class VcsUri {
    private final String vcsTool;
    private final URI repository;
    private final String revision;
    private final File subPath;

    VcsUri(String vcsTool, URI repository, String revision, File subPath) {
        this.vcsTool = vcsTool;
        this.repository = repository;
        this.revision = revision;
        this.subPath = subPath;
    }

    static public VcsUri from(URI uri) {
        try {
            var tool = "";
            var scheme = uri.getScheme();
            final var plusOffset = scheme.indexOf('+');
            if (plusOffset >= 0) {
                tool = scheme.substring(0, plusOffset);
                scheme = scheme.substring(plusOffset + 1);
            }

            var version = (String) null;
            var part = uri.getSchemeSpecificPart();
            final var versionPos = part.indexOf('@');
            if (versionPos >= 0) {
                version = part.substring(versionPos + 1);
                part = part.substring(0, versionPos);
            }
            final URI repository = URI.create(scheme + ':' + part);

            var path = uri.getFragment();

            return new VcsUri(tool, repository, version, (path != null) ? new File(path) : null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a valid download location: " + uri, e);
        }
    }

    public String getVcsTool() {
        return vcsTool;
    }

    public URI getRepository() {
        return repository;
    }

    public Optional<String> getRevision() {
        return Optional.ofNullable(revision);
    }

    public Optional<File> getSubPath() {
        return Optional.ofNullable(subPath);
    }

    public URI toUri() {
        return URI.create(toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VcsUri)) return false;
        VcsUri vcsUri = (VcsUri) o;
        return vcsTool.equals(vcsUri.vcsTool) &&
                repository.equals(vcsUri.repository) &&
                Objects.equals(revision, vcsUri.revision) &&
                Objects.equals(subPath, vcsUri.subPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vcsTool, repository, revision, subPath);
    }

    @Override
    public String toString() {
        var location = vcsTool + "+" + repository;
        if (revision != null) {
            location += "@" + revision;
        }
        if (subPath != null) {
            location += "#" + subPath;
        }
        return location;
    }
}
