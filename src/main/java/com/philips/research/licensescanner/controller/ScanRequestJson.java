/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
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
