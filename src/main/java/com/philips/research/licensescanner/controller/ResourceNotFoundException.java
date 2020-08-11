package com.philips.research.licensescanner.controller;

/**
 * Exception for requested resources that are not available.
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;

    public ResourceNotFoundException(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
