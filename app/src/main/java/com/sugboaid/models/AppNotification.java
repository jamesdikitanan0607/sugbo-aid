package com.sugboaid.models;

import com.google.gson.annotations.SerializedName;

/**
 * Data model representing an app notification
 */
public class AppNotification {
    @SerializedName("id")
    private int id;
    
    @SerializedName("type")
    private NotificationType type;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    @SerializedName("read")
    private boolean read;
    
    @SerializedName("iconResource")
    private String iconResource;
    
    @SerializedName("colorGradient")
    private String colorGradient;
    
    @SerializedName("actionUrl")
    private String actionUrl;

    // Default constructor
    public AppNotification() {
        this.timestamp = System.currentTimeMillis();
        this.read = false;
        this.id = generateId();
    }

    // Constructor with required fields
    public AppNotification(NotificationType type, String title, String message) {
        this();
        this.type = type;
        this.title = title;
        this.message = message;
        setDefaultIconAndColor();
    }

    // Full constructor
    public AppNotification(int id, NotificationType type, String title, String message, 
                          long timestamp, boolean read, String iconResource, String colorGradient) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.read = read;
        this.iconResource = iconResource;
        this.colorGradient = colorGradient;
    }

    // Getters
    public int getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public String getIconResource() {
        return iconResource;
    }

    public String getColorGradient() {
        return colorGradient;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    // Setters with validation
    public void setId(int id) {
        this.id = id;
    }

    public void setType(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        this.type = type;
        setDefaultIconAndColor();
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title.trim();
    }

    public void setMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        this.message = message.trim();
    }

    public void setTimestamp(long timestamp) {
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        this.timestamp = timestamp;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setIconResource(String iconResource) {
        this.iconResource = iconResource;
    }

    public void setColorGradient(String colorGradient) {
        this.colorGradient = colorGradient;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    // Private method to generate unique ID
    private int generateId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    // Private method to set default icon and color based on type
    private void setDefaultIconAndColor() {
        if (type == null) return;
        
        switch (type) {
            case DONATION_RECEIVED:
                this.iconResource = "💝";
                this.colorGradient = "#10b981,#059669"; // Green gradient
                break;
            case INVENTORY_LOW:
                this.iconResource = "⚠️";
                this.colorGradient = "#f59e0b,#d97706"; // Yellow gradient
                break;
            case DISTRIBUTION_COMPLETE:
                this.iconResource = "✅";
                this.colorGradient = "#3b82f6,#2563eb"; // Blue gradient
                break;
            case SYSTEM_UPDATE:
                this.iconResource = "🔄";
                this.colorGradient = "#8b5cf6,#7c3aed"; // Purple gradient
                break;
            case ALERT:
                this.iconResource = "🚨";
                this.colorGradient = "#ef4444,#dc2626"; // Red gradient
                break;
            case INFO:
                this.iconResource = "ℹ️";
                this.colorGradient = "#6b7280,#4b5563"; // Gray gradient
                break;
            default:
                this.iconResource = "📄";
                this.colorGradient = "#6b7280,#4b5563"; // Gray gradient
                break;
        }
    }

    // Utility methods
    public String getFormattedTimestamp() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    public String getTypeDisplayName() {
        switch (type) {
            case DONATION_RECEIVED:
                return "Donation Received";
            case INVENTORY_LOW:
                return "Low Inventory";
            case DISTRIBUTION_COMPLETE:
                return "Distribution Complete";
            case SYSTEM_UPDATE:
                return "System Update";
            case ALERT:
                return "Alert";
            case INFO:
                return "Information";
            default:
                return "Notification";
        }
    }

    public boolean isImportant() {
        return type == NotificationType.ALERT || type == NotificationType.INVENTORY_LOW;
    }

    public void markAsRead() {
        this.read = true;
    }

    public void markAsUnread() {
        this.read = false;
    }

    // Validation method
    public boolean isValid() {
        return type != null &&
               title != null && !title.trim().isEmpty() &&
               message != null && !message.trim().isEmpty() &&
               timestamp > 0;
    }

    // Static factory methods for common notification types
    public static AppNotification createDonationNotification(String donorName, String amount) {
        String title = "New Donation Received";
        String message = String.format("Received %s from %s", amount, donorName);
        return new AppNotification(NotificationType.DONATION_RECEIVED, title, message);
    }

    public static AppNotification createLowInventoryNotification(String itemName, int stock) {
        String title = "Low Inventory Alert";
        String message = String.format("%s is running low (%d remaining)", itemName, stock);
        return new AppNotification(NotificationType.INVENTORY_LOW, title, message);
    }

    public static AppNotification createDistributionNotification(String location, int families) {
        String title = "Distribution Complete";
        String message = String.format("Successfully distributed supplies to %d families in %s", families, location);
        return new AppNotification(NotificationType.DISTRIBUTION_COMPLETE, title, message);
    }

    @Override
    public String toString() {
        return "AppNotification{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", read=" + read +
                ", iconResource='" + iconResource + '\'' +
                ", colorGradient='" + colorGradient + '\'' +
                ", actionUrl='" + actionUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AppNotification that = (AppNotification) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}