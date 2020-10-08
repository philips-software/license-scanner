/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

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
