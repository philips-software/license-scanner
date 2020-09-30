/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import pl.tlinkowski.annotation.basic.NullOr;

import java.net.URI;

class ScanRequestJson {
    @NullOr URI location;
}
