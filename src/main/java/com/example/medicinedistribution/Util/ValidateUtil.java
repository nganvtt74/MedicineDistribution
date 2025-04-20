package com.example.medicinedistribution.Util;

import java.util.regex.Pattern;

public class ValidateUtil {
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Vietnamese phone number pattern (supports multiple formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(?:(?:0|\\+84)\\d{9}|\\+\\d{1,3}(?:[ \\-]\\d{1,4}){2,4})$"
    );

    // Username validation (alphanumeric and underscore, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    // Password validation (at least 8 characters, includes letter and number)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
    );

    /**
     * Validates if the provided string is a valid email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates if the provided string is a valid Vietnamese phone number
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates if the provided string is a valid username
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validates if the provided string is a valid password
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates if the string is not null and not empty
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Validates if a string is within length constraints
     */
    public static boolean isValidLength(String text, int minLength, int maxLength) {
        if (text == null) {
            return minLength == 0;
        }
        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates if the provided string contains only numbers
     */
    public static boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("^[0-9]+$");
    }

    /**
     * Validates if the provided string represents a positive number
     */
    public static boolean isPositiveNumber(String number) {
        try {
            double value = Double.parseDouble(number);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the provided string represents a non-negative number
     */
    public static boolean isNonNegativeNumber(String number) {
        try {
            double value = Double.parseDouble(number);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the provided string contains only letters and spaces
     */
    public static boolean isAlphaWithSpaces(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("^[a-zA-Z\\s]+$");
    }
}