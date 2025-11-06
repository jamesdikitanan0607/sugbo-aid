package com.sugboaid.models;

/**
 * Enum representing the type of transaction
 */
public enum TransactionType {
    DONATION("donation"),
    DISTRIBUTION("distribution"),
    INVENTORY_UPDATE("inventory_update"),
    TRANSFER("transfer");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionType fromString(String value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + value);
    }
}