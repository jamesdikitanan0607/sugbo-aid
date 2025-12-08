package com.sugboaid.donation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for Transparency Dashboard
 * Handles data processing for charts and statistics
 */
public class TransparencyViewModel extends AndroidViewModel {

    private final DonationRepository donationRepository;
    private final MutableLiveData<Double> totalDonations = new MutableLiveData<>(0.0);
    private final MutableLiveData<Integer> totalDistributed = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> totalFamiliesHelped = new MutableLiveData<>(0);
    private final MutableLiveData<List<Entry>> donationTrends = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> distributionData = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> categoryBreakdown = new MutableLiveData<>();
    private final MutableLiveData<List<com.sugboaid.donation.models.BarangayLocation>> barangayLocations = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBarangays = new MutableLiveData<>(0);
    private final MutableLiveData<List<com.sugboaid.donation.models.ImpactStory>> impactStories = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalStories = new MutableLiveData<>(0);

    public TransparencyViewModel(@NonNull Application application) {
        super(application);
        donationRepository = DonationRepository.getInstance(application);
        loadData();
    }

    public LiveData<Double> getTotalDonations() {
        return totalDonations;
    }

    public LiveData<Integer> getTotalDistributed() {
        return totalDistributed;
    }

    public LiveData<Integer> getTotalFamiliesHelped() {
        return totalFamiliesHelped;
    }

    public LiveData<List<Entry>> getDonationTrends() {
        return donationTrends;
    }

    public LiveData<List<BarEntry>> getDistributionData() {
        return distributionData;
    }

    public LiveData<List<PieEntry>> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public LiveData<List<com.sugboaid.donation.models.BarangayLocation>> getBarangayLocations() {
        return barangayLocations;
    }

    public LiveData<Integer> getTotalBarangays() {
        return totalBarangays;
    }

    public LiveData<List<com.sugboaid.donation.models.ImpactStory>> getImpactStories() {
        return impactStories;
    }

    public LiveData<Integer> getTotalStories() {
        return totalStories;
    }

    public void refreshData() {
        loadData();
    }

    public void refreshBarangayData() {
        loadBarangayData();
    }

    public void refreshStoriesData() {
        loadImpactStories();
    }

    private void loadData() {
        // Get donations from SharedPreferences directly for synchronous access
        SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(getApplication());
        List<Donation> donations = prefsHelper.getDonations();
        
        // Calculate total donations
        double total = 0.0;
        int totalItems = 0;
        int families = 0;
        
        for (Donation donation : donations) {
            if (donation.getType() == DonationType.CASH) {
                total += donation.getAmount();
            } else {
                // For goods donations, count items
                totalItems += (int) donation.getAmount();
            }
            
            // Estimate families helped (simplified calculation)
            if (donation.getAmount() > 0) {
                families++;
            }
        }
        
        totalDonations.setValue(total);
        totalDistributed.setValue(totalItems);
        totalFamiliesHelped.setValue(families);
        
        // Generate chart data
        generateDonationTrends(donations);
        generateDistributionData(donations);
        generateCategoryBreakdown(donations);
        
        // Load barangay data
        loadBarangayData();
        
        // Load impact stories
        loadImpactStories();
    }

    private void generateDonationTrends(List<Donation> donations) {
        List<Entry> entries = new ArrayList<>();
        
        // Get last 7 days of data
        Calendar calendar = Calendar.getInstance();
        Map<Integer, Float> dailyTotals = new HashMap<>();
        
        // Initialize with zeros for last 7 days
        for (int i = 6; i >= 0; i--) {
            dailyTotals.put(i, 0f);
        }
        
        // Calculate daily totals
        for (Donation donation : donations) {
            Calendar donationDate = Calendar.getInstance();
            donationDate.setTimeInMillis(donation.getTimestamp());
            
            long daysDiff = (System.currentTimeMillis() - donation.getTimestamp()) / (1000 * 60 * 60 * 24);
            
            if (daysDiff >= 0 && daysDiff < 7) {
                int dayIndex = (int) (6 - daysDiff); // Reverse order for chart
                float currentTotal = dailyTotals.get(dayIndex);
                
                if (donation.getType() == DonationType.CASH) {
                    dailyTotals.put(dayIndex, currentTotal + (float) donation.getAmount());
                } else {
                    // For goods, use estimated value (₱100 per item)
                    dailyTotals.put(dayIndex, currentTotal + (float) donation.getAmount() * 100);
                }
            }
        }
        
        // Convert to chart entries
        for (int i = 0; i < 7; i++) {
            entries.add(new Entry(i, dailyTotals.get(i)));
        }
        
        donationTrends.setValue(entries);
    }

    private void generateDistributionData(List<Donation> donations) {
        List<BarEntry> entries = new ArrayList<>();
        
        // Count items by category
        Map<String, Integer> categoryTotals = new HashMap<>();
        categoryTotals.put("Rice", 0);
        categoryTotals.put("Water", 0);
        categoryTotals.put("Medicine", 0);
        categoryTotals.put("Clothes", 0);
        
        for (Donation donation : donations) {
            if (donation.getType() == DonationType.GOODS) {
                String description = donation.getDescription();
                if (description != null) {
                    // Parse goods description to extract quantities
                    // This is a simplified parsing - in real app, you'd have structured data
                    if (description.contains("Rice")) {
                        categoryTotals.put("Rice", categoryTotals.get("Rice") + (int) donation.getAmount());
                    } else if (description.contains("Water")) {
                        categoryTotals.put("Water", categoryTotals.get("Water") + (int) donation.getAmount());
                    } else if (description.contains("Medicine")) {
                        categoryTotals.put("Medicine", categoryTotals.get("Medicine") + (int) donation.getAmount());
                    } else if (description.contains("Clothes")) {
                        categoryTotals.put("Clothes", categoryTotals.get("Clothes") + (int) donation.getAmount());
                    }
                }
            }
        }
        
        // Convert to chart entries
        entries.add(new BarEntry(0, categoryTotals.get("Rice")));
        entries.add(new BarEntry(1, categoryTotals.get("Water")));
        entries.add(new BarEntry(2, categoryTotals.get("Medicine")));
        entries.add(new BarEntry(3, categoryTotals.get("Clothes")));
        
        distributionData.setValue(entries);
    }

    private void generateCategoryBreakdown(List<Donation> donations) {
        List<PieEntry> entries = new ArrayList<>();
        
        // Count items by category
        Map<String, Integer> categoryTotals = new HashMap<>();
        categoryTotals.put("Rice", 0);
        categoryTotals.put("Water", 0);
        categoryTotals.put("Medicine", 0);
        categoryTotals.put("Clothes", 0);
        
        int totalItems = 0;
        
        for (Donation donation : donations) {
            if (donation.getType() == DonationType.GOODS) {
                String description = donation.getDescription();
                if (description != null) {
                    int amount = (int) donation.getAmount();
                    totalItems += amount;
                    
                    // Parse goods description to extract quantities
                    if (description.contains("Rice")) {
                        categoryTotals.put("Rice", categoryTotals.get("Rice") + amount);
                    } else if (description.contains("Water")) {
                        categoryTotals.put("Water", categoryTotals.get("Water") + amount);
                    } else if (description.contains("Medicine")) {
                        categoryTotals.put("Medicine", categoryTotals.get("Medicine") + amount);
                    } else if (description.contains("Clothes")) {
                        categoryTotals.put("Clothes", categoryTotals.get("Clothes") + amount);
                    }
                }
            }
        }
        
        // Convert to pie entries (only include categories with data)
        if (totalItems > 0) {
            for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
                if (entry.getValue() > 0) {
                    float percentage = (entry.getValue() * 100f) / totalItems;
                    entries.add(new PieEntry(percentage, entry.getKey()));
                }
            }
        } else {
            // Default data if no donations exist
            entries.add(new PieEntry(25f, "Rice"));
            entries.add(new PieEntry(25f, "Water"));
            entries.add(new PieEntry(25f, "Medicine"));
            entries.add(new PieEntry(25f, "Clothes"));
        }
        
        categoryBreakdown.setValue(entries);
    }

    private void loadBarangayData() {
        // Generate sample barangay data based on donations
        List<com.sugboaid.donation.models.BarangayLocation> barangays = new ArrayList<>();
        
        // Sample barangay data for Cebu City
        barangays.add(new com.sugboaid.donation.models.BarangayLocation("Lahug", 10.32424, 123.89835, 45, 25000.0, "active", 12));
        barangays.add(new com.sugboaid.donation.models.BarangayLocation("Capitol Site", 10.32458, 123.89033, 32, 18500.0, "moderate", 8));
        barangays.add(new com.sugboaid.donation.models.BarangayLocation("Guadalupe", 10.3194, 123.8842, 28, 15200.0, "low", 6));
        barangays.add(new com.sugboaid.donation.models.BarangayLocation("Banilad", 10.3479, 123.9132, 38, 22800.0, "active", 10));
        barangays.add(new com.sugboaid.donation.models.BarangayLocation("Talamban", 10.3500, 123.9167, 15, 8900.0, "critical", 3));
        barangays.add(new com.sugboaid.donation.models.BarangayLocation("Mabolo", 10.3136, 123.9136, 41, 24600.0, "active", 11));
        
        barangayLocations.setValue(barangays);
        totalBarangays.setValue(barangays.size());
    }

    private void loadImpactStories() {
        // Generate sample impact stories
        List<com.sugboaid.donation.models.ImpactStory> stories = new ArrayList<>();
        
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        
        stories.add(new com.sugboaid.donation.models.ImpactStory(
            "1",
            "Santos Family",
            "Sitio Mahayag",
            "Barangay Lahug",
            "The Santos family lost their home during the recent typhoon. With 5 children to care for, they were struggling to find shelter and food. Thanks to the generous donations from the community, they received emergency supplies and temporary housing assistance.",
            "• 25kg Rice • 24 bottles Water • Emergency Medicine Kit • Clothing for 5 children",
            currentTime - (5 * dayInMillis),
            7,
            null,
            true
        ));
        
        stories.add(new com.sugboaid.donation.models.ImpactStory(
            "2",
            "Rodriguez Family",
            "Sitio Riverside",
            "Barangay Capitol Site",
            "After losing their livelihood due to the pandemic, the Rodriguez family struggled to provide basic necessities. The community support helped them get back on their feet with food supplies and medical assistance for their elderly grandmother.",
            "• 20kg Rice • Medical supplies • Hygiene kits • School supplies for 3 children",
            currentTime - (12 * dayInMillis),
            5,
            null,
            true
        ));
        
        stories.add(new com.sugboaid.donation.models.ImpactStory(
            "3",
            "Dela Cruz Family",
            "Sitio Kamagayan",
            "Barangay Guadalupe",
            "When Mr. Dela Cruz was hospitalized, the family faced financial difficulties. The community rallied together to provide food and medicine, ensuring the children could continue their education.",
            "• 15kg Rice • 12 bottles Water • Medicine for diabetes • Educational materials",
            currentTime - (8 * dayInMillis),
            4,
            null,
            true
        ));
        
        stories.add(new com.sugboaid.donation.models.ImpactStory(
            "4",
            "Morales Family",
            "Sitio Banawa",
            "Barangay Banilad",
            "A single mother of three, Mrs. Morales works as a street vendor. When her cart was damaged in an accident, the community helped her rebuild her business and provided food for her children.",
            "• 18kg Rice • 18 bottles Water • Clothing • Business capital assistance",
            currentTime - (15 * dayInMillis),
            4,
            null,
            true
        ));
        
        stories.add(new com.sugboaid.donation.models.ImpactStory(
            "5",
            "Garcia Family",
            "Sitio Proper",
            "Barangay Talamban",
            "The Garcia family's house was severely damaged by flooding. With nowhere to go and limited resources, they received emergency shelter materials and food supplies from the relief operations.",
            "• 30kg Rice • 36 bottles Water • Tarpaulin sheets • Emergency clothing",
            currentTime - (20 * dayInMillis),
            6,
            null,
            true
        ));
        
        impactStories.setValue(stories);
        totalStories.setValue(stories.size());
    }
}