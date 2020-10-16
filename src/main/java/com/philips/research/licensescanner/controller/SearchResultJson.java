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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SearchResultJson<T> {
    final List<T> results;

    public SearchResultJson(Stream<T> results) {
        this.results = results.collect(Collectors.toList());
    }
}
