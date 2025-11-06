package com.sugboaid.models;

/**
 * Enum representing the status of inventory items based on stock levels
 */
public enum InventoryStatus {
    HEALTHY("healthy"),
    MODERATE("moderate"),
    LOW("low"),
    CRITICAL("critical");

    private final String value;

    InventoryStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static InventoryStatus fromString(String value) {
        for (InventoryStatus status : InventoryStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown inventory status: " + value);
    }

    /**
     * Determines inventory status based on stock percentage
     * @param currentStock Current stock amount
     * @param capacity Maximum capacity
     * @return Appropriate InventoryStatus
     */
    public static InventoryStatus fromStockLevel(int currentStock, int capacity) {
        if (capacity <= 0) return CRITICAL;
        
        double percentage = (double) currentStock / capacity;
        
        if (percentage >= 0.75) return HEALTHY;
        if (percentage >= 0.50) return MODERATE;
        if (percentage >= 0.25) return LOW;
        return CRITICAL;
    }
}