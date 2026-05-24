package com.eventmacha.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Centralized exception mapper – converts all exceptions to structured JSON error responses.
 * Handles validation errors, domain exceptions, and unexpected errors.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        return switch (exception) {
            case ApiException e -> handleApiException(e);
            case ConstraintViolationException e -> handleValidation(e);
            default -> handleGeneric(exception);
        };
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private Response handleApiException(ApiException e) {
        if (e.getHttpStatus() >= 500) {
            LOG.errorf(e, "Domain error [%s]: %s", e.getErrorCode(), e.getMessage());
        } else {
            LOG.debugf("Client error [%s]: %s", e.getErrorCode(), e.getMessage());
        }
        return Response
                .status(e.getHttpStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiErrorResponse(e.getErrorCode(), e.getMessage()))
                .build();
    }

    private Response handleValidation(ConstraintViolationException e) {
        List<ApiErrorResponse.FieldError> errors = e.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        LOG.debugf("Validation failed: %d violation(s)", errors.size());
        return Response
                .status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiErrorResponse("VALIDATION_ERROR", "Request validation failed", errors))
                .build();
    }

    private Response handleGeneric(Exception e) {
        LOG.errorf(e, "Unhandled exception: %s", e.getMessage());
        return Response
                .status(500)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ApiErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                .build();
    }

    private ApiErrorResponse.FieldError toFieldError(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath().toString();
        // Strip method prefix (e.g. "createOrder.arg0.amount" → "amount")
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new ApiErrorResponse.FieldError(field, cv.getMessage());
    }
}
