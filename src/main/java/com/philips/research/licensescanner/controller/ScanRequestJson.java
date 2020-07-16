package com.philips.research.licensescanner.controller;

import javax.validation.constraints.NotNull;
import java.net.URI;

class ScanRequestJson {
    @NotNull
    URI location;
}
