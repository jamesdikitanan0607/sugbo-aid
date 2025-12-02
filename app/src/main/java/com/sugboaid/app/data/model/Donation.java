package com.sugboaid.app.data.model;

import java.util.List;

public class Donation {
    private String id;
    private String donorId;
    private String donorName;
    private String organizationId;
    private String organizationName;
    private double amount;
    private String currency;
    private String type; // CASH, GOODS, SERVICES
    private String category; // FOOD, CLOTHING, MEDICAL, EDUCATION, etc.
    private String description;
    private List<DonationItem> items;
    private String status; // PENDING, CONFIRMED, DISTRIBUTED
    private String receiptId;
    private String qrCode;
    private long timestamp;
    private String location;
    private String notes;

    public Donation() {
        this.timestamp = System.currentTimeMillis();
        this.status = "PENDING";
        this.currency = "PHP";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<DonationItem> getItems() { return items; }
    public void setItems(List<DonationItem> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}