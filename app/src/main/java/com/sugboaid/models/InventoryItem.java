package com.sugboaid.models;

import com.google.gson.annotations.SerializedName;

/**
 * Data model representing an inventory item
 */
public class InventoryItem {
    @SerializedName("name")
    private String name;
    
    @SerializedName("stock")
    private int stock;
    
    @SerializedName("capacity")
    private int capacity;
    
    @SerializedName("unit")
    private String unit;
    
    @SerializedName("status")
    private InventoryStatus status;
    
    @SerializedName("iconEmoji")
    private String iconEmoji;
    
    @SerializedName("colorGradient")
    private String colorGradient;
    
    @SerializedName("lastUpdated")
    private long lastUpdated;

    // Default constructor
    public InventoryItem() {
        this.lastUpdated = System.currentTimeMillis();
        this.stock = 0;
        this.capacity = 100;
        this.unit = "items";
        updateStatus();
    }

    // Constructor with required fields
    public InventoryItem(String name, int stock, int capacity, String unit) {
        this();
        this.name = name;
        this.stock = stock;
        this.capacity = capacity;
        this.unit = unit;
        updateStatus();
    }

    // Full constructor
    public InventoryItem(String name, int stock, int capacity, String unit, 
                        String iconEmoji, String colorGradient) {
        this(name, stock, capacity, unit);
        this.iconEmoji = iconEmoji;
        this.colorGradient = colorGradient;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getUnit() {
        return unit;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public String getColorGradient() {
        return colorGradient;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters with validation
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = stock;
        this.lastUpdated = System.currentTimeMillis();
        updateStatus();
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        updateStatus();
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit.trim() : "items";
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public void setColorGradient(String colorGradient) {
        this.colorGradient = colorGradient;
    }

    // Private method to update status based on stock level
    private void updateStatus() {
        this.status = InventoryStatus.fromStockLevel(stock, capacity);
    }

    // Utility methods
    public double getStockPercentage() {
        if (capacity <= 0) return 0.0;
        return (double) stock / capacity * 100.0;
    }

    public int getAvailableCapacity() {
        return Math.max(0, capacity - stock);
    }

    public boolean isLowStock() {
        return status == InventoryStatus.LOW || status == InventoryStatus.CRITICAL;
    }

    public boolean isCriticalStock() {
        return status == InventoryStatus.CRITICAL;
    }

    public void addStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add negative stock");
        }
        setStock(Math.min(stock + amount, capacity));
    }

    public void removeStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Cannot remove negative stock");
        }
        setStock(Math.max(0, stock - amount));
    }

    // Validation method
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               stock >= 0 &&
               capacity > 0 &&
               unit != null && !unit.trim().isEmpty();
    }

    public String getFormattedStock() {
        return String.format("%d/%d %s", stock, capacity, unit);
    }

    public String getStatusColor() {
        switch (status) {
            case HEALTHY:
                return "#10b981"; // Green
            case MODERATE:
                return "#f59e0b"; // Yellow
            case LOW:
                return "#f97316"; // Orange
            case CRITICAL:
                return "#ef4444"; // Red
            default:
                return "#6b7280"; // Gray
        }
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "name='" + name + '\'' +
                ", stock=" + stock +
                ", capacity=" + capacity +
                ", unit='" + unit + '\'' +
                ", status=" + status +
                ", iconEmoji='" + iconEmoji + '\'' +
                ", colorGradient='" + colorGradient + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InventoryItem that = (InventoryItem) obj;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}