package com.philips.research.licensescanner.controller;

/**
 * Exception for requested resources that are not available.
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;

    public ResourceNotFoundException(Object resource) {
        this.resource = (resource != null) ? resource.toString() : "?";
    }

    public String getResource() {
        return resource;
    }
}
