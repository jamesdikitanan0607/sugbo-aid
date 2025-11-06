package com.sugboaid.donation.models;

/**
 * Model class representing a barangay location with donation information
 */
public class BarangayLocation {
    private String name;
    private double latitude;
    private double longitude;
    private int familiesHelped;
    private double totalDonations;
    private String status; // "active", "moderate", "low", "critical"
    private int donationCount;

    public BarangayLocation() {
        // Default constructor
    }

    public BarangayLocation(String name, double latitude, double longitude, 
                           int familiesHelped, double totalDonations, String status, int donationCount) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.familiesHelped = familiesHelped;
        this.totalDonations = totalDonations;
        this.status = status;
        this.donationCount = donationCount;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getFamiliesHelped() {
        return familiesHelped;
    }

    public void setFamiliesHelped(int familiesHelped) {
        this.familiesHelped = familiesHelped;
    }

    public double getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(double totalDonations) {
        this.totalDonations = totalDonations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDonationCount() {
        return donationCount;
    }

    public void setDonationCount(int donationCount) {
        this.donationCount = donationCount;
    }

    /**
     * Get status color resource based on status
     */
    public int getStatusColorResource() {
        switch (status.toLowerCase()) {
            case "active":
                return com.sugboaid.donation.R.color.status_healthy;
            case "moderate":
                return com.sugboaid.donation.R.color.status_moderate;
            case "low":
                return com.sugboaid.donation.R.color.status_low;
            case "critical":
                return com.sugboaid.donation.R.color.status_critical;
            default:
                return com.sugboaid.donation.R.color.medium_gray;
        }
    }

    /**
     * Get formatted donation amount
     */
    public String getFormattedDonations() {
        return String.format("₱%,.2f", totalDonations);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BarangayLocation that = (BarangayLocation) obj;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BarangayLocation{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", familiesHelped=" + familiesHelped +
                ", totalDonations=" + totalDonations +
                ", status='" + status + '\'' +
                ", donationCount=" + donationCount +
                '}';
    }
}