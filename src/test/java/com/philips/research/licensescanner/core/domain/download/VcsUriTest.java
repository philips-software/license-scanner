package com.philips.research.licensescanner.core.domain.download;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class VcsUriTest {
    @Test
    void parsesPlainLocation() {
        var location = VcsUri.from(URI.create("git+https://example.com/blah@revision"));

        assertThat(location.getVcsTool()).isEqualTo("git");
        assertThat(location.getRepository()).isEqualTo(URI.create("https://example.com/blah"));
        assertThat(location.getRevision()).contains("revision");
        assertThat(location.getSubPath()).isEmpty();
    }

    @Test
    void parsesLocationWithoutRevision() {
        var location = VcsUri.from(URI.create("git+https://example.com/blah#sub/path"));

        assertThat(location.getRevision()).isEmpty();
        assertThat(location.getSubPath()).contains(new File("sub/path"));
    }

    @Test
    void parsesFullLocation() {
        var location = VcsUri.from(URI.create("git+https://example.com/blah@revision#sub/path"));

        assertThat(location.getRevision()).contains("revision");
        assertThat(location.getSubPath()).contains(new File("sub/path"));
    }

    @Test
    void formatsLocationAsString() {
        var plain = URI.create("tool+ssh://example.com@version");
        var withPath = URI.create("tool+ssh://example.com@version#path");
        var withoutRevision = URI.create("tool+ssh://example.com#path");

        assertThat(VcsUri.from(plain).toString()).isEqualTo(plain.toString());
        assertThat(VcsUri.from(withPath).toString()).isEqualTo(withPath.toString());
        assertThat(VcsUri.from(withoutRevision).toString()).isEqualTo(withoutRevision.toString());
    }

    @Test
    void implementsEquality() {
        EqualsVerifier.forClass(VcsUri.class)
                .withNonnullFields("vcsTool", "repository")
                .verify();
    }
}

