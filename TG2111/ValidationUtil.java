package com.fot.ams.util;

import com.fot.ams.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Validates user inputs across the application.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+94|0)[0-9]{9}$"
    );

    private ValidationUtil() {
    }

    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        validateNotEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address (e.g., user@fot.ruh.ac.lk).");
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        if (phone != null && !phone.trim().isEmpty()) {
            String sanitized = phone.replaceAll("\\s+", "");
            if (!PHONE_PATTERN.matcher(sanitized).matches()) {
                throw new ValidationException("Please enter a valid 10-digit phone number (e.g., 0771234567 or +94771234567).");
            }
        }
    }

    public static void validateMark(double mark) throws ValidationException {
        if (mark < 0.0 || mark > 100.0) {
            throw new ValidationException("Please enter a valid mark between 0 and 100.");
        }
    }

    public static void validatePositive(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer.");
        }
    }

    public static void validateSelection(Object selectedItem, String fieldName) throws ValidationException {
        if (selectedItem == null || selectedItem.toString().trim().isEmpty() || selectedItem.toString().startsWith("--")) {
            throw new ValidationException("Please select a valid " + fieldName + ".");
        }
    }
}
