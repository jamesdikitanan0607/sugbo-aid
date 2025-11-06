package com.sugboaid.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.UUID;

/**
 * Data model representing a transaction record
 */
public class Transaction {
    @SerializedName("id")
    private String id;
    
    @SerializedName("donor")
    private String donor;
    
    @SerializedName("type")
    private TransactionType type;
    
    @SerializedName("amount")
    private String amount;
    
    @SerializedName("date")
    private Date date;
    
    @SerializedName("campaign")
    private String campaign;
    
    @SerializedName("verified")
    private boolean verified;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("receiptId")
    private String receiptId;

    // Default constructor
    public Transaction() {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
        this.verified = false;
        this.receiptId = generateReceiptId();
    }

    // Constructor with required fields
    public Transaction(String donor, TransactionType type, String amount) {
        this();
        this.donor = donor;
        this.type = type;
        this.amount = amount;
    }

    // Full constructor
    public Transaction(String id, String donor, TransactionType type, String amount, 
                      Date date, String campaign, boolean verified, String description) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.donor = donor;
        this.type = type;
        this.amount = amount;
        this.date = date != null ? date : new Date();
        this.campaign = campaign;
        this.verified = verified;
        this.description = description;
        this.receiptId = generateReceiptId();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDonor() {
        return donor;
    }

    public TransactionType getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getCampaign() {
        return campaign;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getDescription() {
        return description;
    }

    public String getReceiptId() {
        return receiptId;
    }

    // Setters with validation
    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        this.id = id;
    }

    public void setDonor(String donor) {
        this.donor = donor != null ? donor.trim() : "Anonymous";
    }

    public void setType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        this.type = type;
    }

    public void setAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be null or empty");
        }
        this.amount = amount.trim();
    }

    public void setDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign != null ? campaign.trim() : "";
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    // Private method to generate receipt ID
    private String generateReceiptId() {
        return "RCP-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Utility methods
    public long getTimestamp() {
        return date != null ? date.getTime() : 0;
    }

    public String getFormattedDate() {
        if (date == null) return "";
        return android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", date).toString();
    }

    public String getTypeIcon() {
        switch (type) {
            case DONATION:
                return "💝";
            case DISTRIBUTION:
                return "📦";
            case INVENTORY_UPDATE:
                return "📊";
            case TRANSFER:
                return "🔄";
            default:
                return "📄";
        }
    }

    public String getTypeColor() {
        switch (type) {
            case DONATION:
                return "#10b981"; // Green
            case DISTRIBUTION:
                return "#3b82f6"; // Blue
            case INVENTORY_UPDATE:
                return "#f59e0b"; // Yellow
            case TRANSFER:
                return "#8b5cf6"; // Purple
            default:
                return "#6b7280"; // Gray
        }
    }

    // Validation method
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               type != null &&
               amount != null && !amount.trim().isEmpty() &&
               date != null;
    }

    // Method to create transaction from donation
    public static Transaction fromDonation(Donation donation) {
        if (donation == null) {
            throw new IllegalArgumentException("Donation cannot be null");
        }
        
        Transaction transaction = new Transaction();
        transaction.setDonor(donation.getDonorName());
        transaction.setType(TransactionType.DONATION);
        transaction.setAmount(donation.getFormattedAmount());
        transaction.setDate(new Date(donation.getTimestamp()));
        transaction.setCampaign(donation.getCampaign());
        transaction.setVerified(donation.isVerified());
        transaction.setDescription(donation.getDescription());
        
        return transaction;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", donor='" + donor + '\'' +
                ", type=" + type +
                ", amount='" + amount + '\'' +
                ", date=" + date +
                ", campaign='" + campaign + '\'' +
                ", verified=" + verified +
                ", description='" + description + '\'' +
                ", receiptId='" + receiptId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction that = (Transaction) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}