/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.licensescanner.controller;

/**
 * Exception for requested resources that are not available.
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;

    public ResourceNotFoundException(Object resource) {
        this.resource = resource.toString();
    }

    public String getResource() {
        return resource;
    }
}
