/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

import com.philips.research.licensescanner.core.LicenseService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SearchResultJson<T> {
    final List<T> results;
    final int licenses;
    final int errors;
    final int contested;

    public SearchResultJson(LicenseService.StatisticsDto stats, Stream<T> results) {
        licenses = stats.licenses;
        errors = stats.errors;
        contested = stats.contested;
        this.results = results.collect(Collectors.toList());
    }
}
