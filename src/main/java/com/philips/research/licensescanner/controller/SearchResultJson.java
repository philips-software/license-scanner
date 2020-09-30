/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SearchResultJson {
    final List<PackageInfoJson> results;

    public SearchResultJson(Stream<PackageInfoJson> results) {
        this.results = results.collect(Collectors.toList());
    }

    public SearchResultJson() {
        results = List.of();
    }
}
