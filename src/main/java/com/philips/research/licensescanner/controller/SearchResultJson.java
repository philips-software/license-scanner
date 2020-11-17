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
