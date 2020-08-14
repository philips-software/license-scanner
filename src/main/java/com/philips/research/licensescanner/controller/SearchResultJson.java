package com.philips.research.licensescanner.controller;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Validated
class SearchResultJson {
    List<ScanInfoJson> results;

    public SearchResultJson(Stream<ScanInfoJson> results) {
        this.results = results.collect(Collectors.toList());
    }

    public SearchResultJson() {
        results = List.of();
    }
}
