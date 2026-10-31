package com.fot.ams.exception;

/**
 * Custom exception for user authentication failures.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
