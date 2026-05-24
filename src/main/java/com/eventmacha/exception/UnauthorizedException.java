package com.eventmacha.exception;

/** 401 Unauthorized – invalid or missing JWT. */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(401, "UNAUTHORIZED", message);
    }
    public UnauthorizedException(String message, Throwable cause) {
        super(401, "UNAUTHORIZED", message, cause);
    }
}
