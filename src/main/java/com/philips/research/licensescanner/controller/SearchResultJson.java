package com.philips.research.licensescanner.controller;

import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
class SearchResultJson {
    List<ScanInfoJson> results;
}
