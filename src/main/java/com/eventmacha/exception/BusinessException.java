package com.eventmacha.exception;

/** 422 Unprocessable Entity – business rule violation. */
public class BusinessException extends ApiException {
    public BusinessException(String errorCode, String message) {
        super(422, errorCode, message);
    }
    public BusinessException(String message) {
        super(422, "BUSINESS_ERROR", message);
    }
}
