package com.fot.ams.exception;

/**
 * Custom exception for input validation errors.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
