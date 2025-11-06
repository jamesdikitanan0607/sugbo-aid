package com.sugboaid.models;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

/**
 * Data model representing a donation record
 */
public class Donation implements java.io.Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("donorName")
    private String donorName;
    
    @SerializedName("type")
    private DonationType type;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    @SerializedName("campaign")
    private String campaign;
    
    @SerializedName("verified")
    private boolean verified;

    // Default constructor
    public Donation() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.verified = false;
    }

    // Constructor with required fields
    public Donation(String donorName, DonationType type, double amount) {
        this();
        this.donorName = donorName;
        this.type = type;
        this.amount = amount;
    }

    // Full constructor
    public Donation(String id, String donorName, DonationType type, double amount, 
                   String description, long timestamp, String campaign, boolean verified) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.donorName = donorName;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.campaign = campaign;
        this.verified = verified;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDonorName() {
        return donorName;
    }

    public DonationType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCampaign() {
        return campaign;
    }

    public boolean isVerified() {
        return verified;
    }

    // Setters with validation
    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        this.id = id;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName != null ? donorName.trim() : "Anonymous";
    }

    public void setType(DonationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Donation type cannot be null");
        }
        this.type = type;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public void setTimestamp(long timestamp) {
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        this.timestamp = timestamp;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign != null ? campaign.trim() : "";
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // Validation method
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               type != null &&
               amount >= 0 &&
               timestamp > 0;
    }

    // Utility methods
    public String getFormattedAmount() {
        if (type == DonationType.CASH) {
            return String.format("₱%.2f", amount);
        } else {
            return String.format("%.0f items", amount);
        }
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id='" + id + '\'' +
                ", donorName='" + donorName + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", campaign='" + campaign + '\'' +
                ", verified=" + verified +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Donation donation = (Donation) obj;
        return id != null ? id.equals(donation.id) : donation.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}