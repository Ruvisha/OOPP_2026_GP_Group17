package com.fot.ams.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying user passwords using SHA-256.
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hashes a plain password using SHA-256.
     *
     * @param plainPassword plain text password
     * @return 64-character lowercase hex string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies plain text password against stored hash (or plain text match).
     *
     * @param plainPassword input password
     * @param storedPassword hash or plain password from database
     * @return true if valid
     */
    public static boolean verifyPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equalsIgnoreCase(storedPassword) || plainPassword.equals(storedPassword);
    }

    /**
     * Validates whether a password meets the security requirements:
     * 1. Minimum password length must be exactly 8 characters or more.
     * 2. Must contain at least one uppercase letter (A-Z).
     * 3. Must contain at least one lowercase letter (a-z).
     * 4. Must contain at least one number (0-9).
     * 5. Special symbols are optional.
     * 6. Passwords shorter than 8 characters are rejected.
     *
     * @param password plain text password to validate
     * @return true if password meets all requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.trim().length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                hasUpper = true;
            } else if (c >= 'a' && c <= 'z') {
                hasLower = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            }
        }
        return hasUpper && hasLower && hasDigit;
    }
}
