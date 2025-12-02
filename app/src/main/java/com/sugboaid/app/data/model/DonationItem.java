package com.sugboaid.app.data.model;

public class DonationItem {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private String unit;
    private double unitValue;
    private double totalValue;
    private String condition; // NEW, GOOD, FAIR
    private String description;
    private String imageUrl;

    public DonationItem() {}

    public DonationItem(String name, String category, int quantity, String unit, double unitValue) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.unitValue = unitValue;
        this.totalValue = quantity * unitValue;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        this.totalValue = quantity * unitValue;
    }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getUnitValue() { return unitValue; }
    public void setUnitValue(double unitValue) { 
        this.unitValue = unitValue;
        this.totalValue = quantity * unitValue;
    }

    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}