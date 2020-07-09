package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
class SearchResultJson {
    @JsonProperty("results")
    List<ScanInfoJson> results;
}
