package com.philips.research.licensescanner.core.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PackageTest {
    @Test
    void implementsEquals() {
        EqualsVerifier.forClass(Package.class)
                .withNonnullFields("namespace", "name", "version")
                .verify();
    }
}
