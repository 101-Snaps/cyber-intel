package com.nicasia.cyberintel.util;

import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    // Remove HTML tags and dangerous characters
    public String sanitize(String input) {
        if (input == null) return null;

        return input
            .replaceAll("<[^>]*>", "")           // Remove HTML tags
            .replaceAll("[<>\"'%;()&+]", "")     // Remove dangerous chars
            .replaceAll("(?i)(script|select|insert|update|delete|drop|"
                + "union|exec|execute|xp_)", "") // Remove SQL keywords
            .trim();
    }

    // Validate email format
    public boolean isValidEmail(String email) {
        return email != null &&
            email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Validate phone number
    public boolean isValidPhone(String phone) {
        return phone != null &&
            phone.matches("^[0-9+\\-\\s]{7,15}$");
    }

    // Validate SA ID number (13 digits)
    public boolean isValidIdNumber(String idNumber) {
        return idNumber != null &&
            idNumber.matches("^[0-9]{13}$");
    }

    // Check for SQL injection patterns
    public boolean containsSqlInjection(String input) {
        if (input == null) return false;
        String lower = input.toLowerCase();
        return lower.contains("select ") || lower.contains("drop ")
            || lower.contains("insert ") || lower.contains("delete ")
            || lower.contains("union ") || lower.contains("exec(")
            || lower.contains("execute(") || lower.contains("--")
            || lower.contains("xp_");
    }
}