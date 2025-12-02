package com.sugboaid.app.data.model;

public class InventoryItem {
    private String id;
    private String name;
    private String category;
    private int currentStock;
    private int minimumStock;
    private int maximumStock;
    private String unit;
    private double unitValue;
    private String location;
    private String condition;
    private long lastUpdated;
    private String updatedBy;
    private String description;
    private String imageUrl;
    private boolean isLowStock;

    public InventoryItem() {
        this.lastUpdated = System.currentTimeMillis();
    }

    public InventoryItem(String name, String category, int currentStock, String unit) {
        this();
        this.name = name;
        this.category = category;
        this.currentStock = currentStock;
        this.unit = unit;
        updateLowStockStatus();
    }

    public void updateLowStockStatus() {
        this.isLowStock = currentStock <= minimumStock;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { 
        this.currentStock = currentStock;
        updateLowStockStatus();
    }

    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { 
        this.minimumStock = minimumStock;
        updateLowStockStatus();
    }

    public int getMaximumStock() { return maximumStock; }
    public void setMaximumStock(int maximumStock) { this.maximumStock = maximumStock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getUnitValue() { return unitValue; }
    public void setUnitValue(double unitValue) { this.unitValue = unitValue; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isLowStock() { return isLowStock; }
    public void setLowStock(boolean lowStock) { isLowStock = lowStock; }
}