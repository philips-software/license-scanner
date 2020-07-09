package com.philips.research.licensescanner.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.net.URI;

class ScanRequestJson {
    @JsonProperty("vcsUri")
    @NotNull
    URI vcsUri;
}
