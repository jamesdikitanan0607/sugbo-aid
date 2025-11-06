package com.sugboaid.donation.models;

/**
 * Model class representing an impact story with family information and assistance details
 */
public class ImpactStory {
    private String id;
    private String familyName;
    private String location;
    private String barangay;
    private String story;
    private String assistanceReceived;
    private long dateAssisted;
    private int familyMembers;
    private String imageUrl; // For future image loading
    private boolean verified;

    public ImpactStory() {
        // Default constructor
    }

    public ImpactStory(String id, String familyName, String location, String barangay, 
                      String story, String assistanceReceived, long dateAssisted, 
                      int familyMembers, String imageUrl, boolean verified) {
        this.id = id;
        this.familyName = familyName;
        this.location = location;
        this.barangay = barangay;
        this.story = story;
        this.assistanceReceived = assistanceReceived;
        this.dateAssisted = dateAssisted;
        this.familyMembers = familyMembers;
        this.imageUrl = imageUrl;
        this.verified = verified;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getAssistanceReceived() {
        return assistanceReceived;
    }

    public void setAssistanceReceived(String assistanceReceived) {
        this.assistanceReceived = assistanceReceived;
    }

    public long getDateAssisted() {
        return dateAssisted;
    }

    public void setDateAssisted(long dateAssisted) {
        this.dateAssisted = dateAssisted;
    }

    public int getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(int familyMembers) {
        this.familyMembers = familyMembers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Get formatted date string
     */
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(dateAssisted));
    }

    /**
     * Get full location string
     */
    public String getFullLocation() {
        return location + ", " + barangay;
    }

    /**
     * Get family size description
     */
    public String getFamilySizeDescription() {
        return familyMembers + " family members";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ImpactStory that = (ImpactStory) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ImpactStory{" +
                "id='" + id + '\'' +
                ", familyName='" + familyName + '\'' +
                ", location='" + location + '\'' +
                ", barangay='" + barangay + '\'' +
                ", story='" + story + '\'' +
                ", assistanceReceived='" + assistanceReceived + '\'' +
                ", dateAssisted=" + dateAssisted +
                ", familyMembers=" + familyMembers +
                ", imageUrl='" + imageUrl + '\'' +
                ", verified=" + verified +
                '}';
    }
}