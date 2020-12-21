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
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Validated
@CrossOrigin(origins = "*")
public class AbstractRoute {
    protected final LicenseService service;

    public AbstractRoute(LicenseService service) {
        this.service = service;
    }

    protected static URI decodePackageUrl(String purl) {
        try {
            final var decoded = URLDecoder.decode(purl, StandardCharsets.UTF_8);
            return URI.create(decoded);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not decode package URL '" + purl + "'");
        }
    }
}
