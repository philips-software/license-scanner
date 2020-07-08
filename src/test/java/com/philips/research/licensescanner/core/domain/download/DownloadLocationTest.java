package com.philips.research.licensescanner.core.domain.download;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadLocationTest {
    @Test
    void parsesPlainLocation() {
        var location = DownloadLocation.parse("git+https://example.com/blah@revision");

        assertThat(location.getVcsTool()).isEqualTo("git");
        assertThat(location.getRepositoryUrl()).isEqualTo(URI.create("https://example.com/blah"));
        assertThat(location.getRevision()).contains("revision");
        assertThat(location.getSubPath()).isEmpty();
    }

    @Test
    void parsesLocationWithoutRevision() {
        var location = DownloadLocation.parse("git+https://example.com/blah#sub/path");

        assertThat(location.getRevision()).isEmpty();
        assertThat(location.getSubPath()).contains(new File("sub/path"));
    }

    @Test
    void parsesFullLocation() {
        var location = DownloadLocation.parse("git+https://example.com/blah@revision#sub/path");

        assertThat(location.getRevision()).contains("revision");
        assertThat(location.getSubPath()).contains(new File("sub/path"));
    }

    @Test
    void formatsLocationAsString() {
        var plain = "tool+ssh://example.com@version";
        var withPath = "tool+ssh://example.com@version#path";
        var withoutRevision = "tool+ssh://example.com#path";

        assertThat(DownloadLocation.parse(plain).toString()).isEqualTo(plain);
        assertThat(DownloadLocation.parse(withPath).toString()).isEqualTo(withPath);
        assertThat(DownloadLocation.parse(withoutRevision).toString()).isEqualTo(withoutRevision);
    }
}
