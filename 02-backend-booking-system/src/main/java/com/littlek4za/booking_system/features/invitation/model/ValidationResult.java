package com.littlek4za.booking_system.features.invitation.model;

import com.littlek4za.booking_system.exception.model.ErrorCode;

public class ValidationResult {

    private final boolean valid;
    private final String message;
    private final ErrorCode errorCode;

    private ValidationResult(boolean valid, String message, ErrorCode errorCode) {
        this.valid = valid;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult fail(String message, ErrorCode errorCode) {
        return new ValidationResult(false, message, errorCode);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
