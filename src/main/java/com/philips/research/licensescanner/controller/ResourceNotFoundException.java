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
