package com.sugboaid.models;

/**
 * Enum representing the type of notification
 */
public enum NotificationType {
    DONATION_RECEIVED("donation_received"),
    INVENTORY_LOW("inventory_low"),
    DISTRIBUTION_COMPLETE("distribution_complete"),
    SYSTEM_UPDATE("system_update"),
    ALERT("alert"),
    INFO("info");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationType fromString(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }
}