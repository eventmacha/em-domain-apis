package com.eventmacha.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Structured error payload returned for all failure responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final boolean success = false;
    private final String errorCode;
    private final String message;
    private final List<FieldError> errors;
    private final long timestamp;

    public ApiErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.errors = null;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public ApiErrorResponse(String errorCode, String message, List<FieldError> errors) {
        this.errorCode = errorCode;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now().toEpochMilli();
    }

    // ── Nested ────────────────────────────────────────────────────────────────

    public record FieldError(String field, String message) {}

    // ── Accessors ─────────────────────────────────────────────────────────────

    public boolean isSuccess() { return success; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public List<FieldError> getErrors() { return errors; }
    public long getTimestamp() { return timestamp; }
}
