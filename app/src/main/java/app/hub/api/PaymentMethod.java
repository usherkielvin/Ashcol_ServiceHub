package app.hub.api;

public enum PaymentMethod {
    CASH("cash", "Cash"),
    CREDIT_CARD("credit_card", "Credit Card"),
    GPAY("gpay", "Google Pay"),
    BANK_TRANSFER("bank_transfer", "Bank Transfer"),
    ONLINE("online", "Online");

    private final String value;
    private final String displayName;

    PaymentMethod(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String value) {
        if (value == null) {
            return CASH; // Default
        }
        
        for (PaymentMethod method : values()) {
            if (method.value.equalsIgnoreCase(value) || method.name().equalsIgnoreCase(value)) {
                return method;
            }
        }
        
        // Default to CASH if unknown
        return CASH;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
