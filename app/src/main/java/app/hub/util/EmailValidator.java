package app.hub.util;

import android.util.Patterns;
import java.util.regex.Pattern;

public class EmailValidator {
    
    // Enhanced email pattern for stricter validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Validate email format
     */
    public static boolean isValidFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Enhanced email format validation
     */
    public static boolean isValidFormatStrict(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Get email domain
     */
    public static String getDomain(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(email.indexOf("@") + 1);
    }

    /**
     * Validate email domain format
     */
    public static boolean isValidDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }
        // Check if domain has at least one dot and valid TLD
        Pattern domainPattern = Pattern.compile(
            "^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            Pattern.CASE_INSENSITIVE
        );
        return domainPattern.matcher(domain).matches();
    }

    /**
     * Check if email looks valid (format + domain check)
     */
    public static ValidationResult validate(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email is required");
        }

        if (!isValidFormat(email)) {
            return new ValidationResult(false, "Invalid email format");
        }

        String domain = getDomain(email);
        if (domain == null || !isValidDomain(domain)) {
            return new ValidationResult(false, "Invalid email domain");
        }

        // Common fake/invalid domains check
        String[] invalidDomains = {"example.com", "test.com", "invalid.com"};
        for (String invalid : invalidDomains) {
            if (domain.equalsIgnoreCase(invalid)) {
                return new ValidationResult(false, "Please use a real email address");
            }
        }

        return new ValidationResult(true, "Valid email");
    }

    public static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}

