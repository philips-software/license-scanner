/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.licensescanner.controller;

import pl.tlinkowski.annotation.basic.NullOr;

import javax.validation.constraints.NotNull;
import java.net.URI;

class ScanRequestJson {
    @NotNull
    URI purl;
    @NullOr URI location;
}
