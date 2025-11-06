package com.sugboaid.models;

/**
 * Enum representing the type of donation
 */
public enum DonationType {
    CASH("cash"),
    GOODS("goods");

    private final String value;

    DonationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DonationType fromString(String value) {
        for (DonationType type : DonationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown donation type: " + value);
    }
}