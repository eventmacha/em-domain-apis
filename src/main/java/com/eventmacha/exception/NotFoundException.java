package com.eventmacha.exception;

/** 404 Not Found. */
public class NotFoundException extends ApiException {
    public NotFoundException(String resource, String id) {
        super(404, "NOT_FOUND", resource + " not found with id: " + id);
    }
    public NotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }
}
