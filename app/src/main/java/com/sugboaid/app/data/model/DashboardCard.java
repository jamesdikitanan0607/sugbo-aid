package com.sugboaid.app.data.model;

public class DashboardCard {
    private String title;
    private String value;
    private String subtitle;
    private int iconResource;
    private int colorResource;

    public DashboardCard() {}

    public DashboardCard(String title, String value, String subtitle, int iconResource, int colorResource) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.iconResource = iconResource;
        this.colorResource = colorResource;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public int getIconResource() { return iconResource; }
    public void setIconResource(int iconResource) { this.iconResource = iconResource; }

    public int getColorResource() { return colorResource; }
    public void setColorResource(int colorResource) { this.colorResource = colorResource; }
}