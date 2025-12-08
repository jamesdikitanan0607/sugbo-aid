package com.sugboaid.donation.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.sugboaid.models.Donation;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.repositories.InventoryRepository;
import com.sugboaid.repositories.PreferencesRepository;

import java.util.List;

/**
 * ViewModel for Dashboard screen managing statistics and recent activities
 */
public class DashboardViewModel extends AndroidViewModel {
    
    private DonationRepository donationRepository;
    private InventoryRepository inventoryRepository;
    private PreferencesRepository preferencesRepository;
    
    // LiveData for statistics
    private LiveData<Double> totalDonations;
    private LiveData<Integer> distributedItems;
    private LiveData<Integer> familiesHelped;
    private LiveData<List<Donation>> recentActivities;
    
    // LiveData for percentage changes
    private MutableLiveData<String> donationsPercentageChange;
    private MutableLiveData<String> itemsPercentageChange;
    private MutableLiveData<String> familiesPercentageChange;
    
    // Loading states
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    
    // Combined statistics LiveData
    private MediatorLiveData<DashboardStatistics> dashboardStatistics;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        
        // Initialize repositories
        donationRepository = DonationRepository.getInstance(application);
        inventoryRepository = InventoryRepository.getInstance(application);
        preferencesRepository = PreferencesRepository.getInstance(application);
        
        // Initialize LiveData
        initializeLiveData();
        
        // Load initial data
        refreshData();
    }

    private void initializeLiveData() {
        // Statistics from repositories
        totalDonations = donationRepository.getTotalDonations();
        distributedItems = inventoryRepository.getTotalDistributedItems();
        familiesHelped = donationRepository.getTotalFamiliesHelped();
        recentActivities = donationRepository.getRecentDonations();
        
        // Percentage changes (initially 0%)
        donationsPercentageChange = new MutableLiveData<>("+0%");
        itemsPercentageChange = new MutableLiveData<>("+0%");
        familiesPercentageChange = new MutableLiveData<>("+0%");
        
        // Loading and error states
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        
        // Combined statistics - initialize with default values to ensure immediate emission
        dashboardStatistics = new MediatorLiveData<>();
        // Set initial default statistics so UI can render immediately
        dashboardStatistics.setValue(new DashboardStatistics(0.0, 0, 0, "+0%", "+0%", "+0%"));
        setupDashboardStatistics();
    }

    private void setupDashboardStatistics() {
        dashboardStatistics.addSource(totalDonations, donations -> {
            updateDashboardStatistics();
        });
        
        dashboardStatistics.addSource(distributedItems, items -> {
            updateDashboardStatistics();
        });
        
        dashboardStatistics.addSource(familiesHelped, families -> {
            updateDashboardStatistics();
        });
    }

    private void updateDashboardStatistics() {
        Double donations = totalDonations.getValue();
        Integer items = distributedItems.getValue();
        Integer families = familiesHelped.getValue();
        
        if (donations != null && items != null && families != null) {
            DashboardStatistics stats = new DashboardStatistics(
                donations,
                items,
                families,
                donationsPercentageChange.getValue(),
                itemsPercentageChange.getValue(),
                familiesPercentageChange.getValue()
            );
            dashboardStatistics.setValue(stats);
        }
    }

    // Public getters for LiveData
    public LiveData<Double> getTotalDonations() {
        return totalDonations;
    }

    public LiveData<Integer> getDistributedItems() {
        return distributedItems;
    }

    public LiveData<Integer> getFamiliesHelped() {
        return familiesHelped;
    }

    public LiveData<List<Donation>> getRecentActivities() {
        return recentActivities;
    }

    public LiveData<String> getDonationsPercentageChange() {
        return donationsPercentageChange;
    }

    public LiveData<String> getItemsPercentageChange() {
        return itemsPercentageChange;
    }

    public LiveData<String> getFamiliesPercentageChange() {
        return familiesPercentageChange;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<DashboardStatistics> getDashboardStatistics() {
        return dashboardStatistics;
    }

    /**
     * Force a refresh and re-emit current values to observers even if repositories
     * result in unchanged underlying LiveData. Useful after flows like signup where
     * fragment lifecycle timing might skip the initial emission.
     */
    public void forceRefresh() {
        try {
            // Attempt a regular refresh first
            refreshData();
        } catch (Exception ignore) {
            // Continue to re-emit below
        }
        // Re-emit combined statistics to trigger observers
        updateDashboardStatistics();
        // Maintain consistent loading state
        if (isLoading.getValue() != null && isLoading.getValue()) {
            isLoading.setValue(false);
        }
    }

    // Formatted values for UI display
    public LiveData<String> getFormattedTotalDonations() {
        return Transformations.map(totalDonations, amount -> {
            if (amount == null) return "₱0.00";
            return String.format("₱%,.2f", amount);
        });
    }

    public LiveData<String> getFormattedDistributedItems() {
        return Transformations.map(distributedItems, items -> {
            if (items == null) return "0";
            return String.format("%,d", items);
        });
    }

    public LiveData<String> getFormattedFamiliesHelped() {
        return Transformations.map(familiesHelped, families -> {
            if (families == null) return "0";
            return String.format("%,d", families);
        });
    }

    // Data operations
    public void refreshData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        try {
            // Refresh repositories
            donationRepository.refreshData();
            inventoryRepository.refreshData();
            
            // Calculate percentage changes
            calculatePercentageChanges();
            
            isLoading.setValue(false);
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("Failed to refresh data: " + e.getMessage());
        }
    }
    
    public void refreshRecentActivities(String userId) {
        if (userId != null) {
            recentActivities = donationRepository.getRecentDonations(userId);
            updateDashboardStatistics();
        }
    }

    private void calculatePercentageChanges() {
        // Get previous values from preferences
        double previousDonations = preferencesRepository.getPreviousTotalDonations();
        int previousItems = preferencesRepository.getPreviousDistributedItems();
        int previousFamilies = preferencesRepository.getPreviousFamiliesHelped();
        
        // Calculate current values
        Double currentDonations = totalDonations.getValue();
        Integer currentItems = distributedItems.getValue();
        Integer currentFamilies = familiesHelped.getValue();
        
        if (currentDonations != null) {
            String donationsChange = calculatePercentageChange(previousDonations, currentDonations);
            donationsPercentageChange.setValue(donationsChange);
            
            // Save current as previous for next calculation
            preferencesRepository.savePreviousTotalDonations(currentDonations);
        }
        
        if (currentItems != null) {
            String itemsChange = calculatePercentageChange(previousItems, currentItems);
            itemsPercentageChange.setValue(itemsChange);
            
            preferencesRepository.savePreviousDistributedItems(currentItems);
        }
        
        if (currentFamilies != null) {
            String familiesChange = calculatePercentageChange(previousFamilies, currentFamilies);
            familiesPercentageChange.setValue(familiesChange);
            
            preferencesRepository.savePreviousFamiliesHelped(currentFamilies);
        }
    }

    private String calculatePercentageChange(double previous, double current) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        
        double change = ((current - previous) / previous) * 100;
        String sign = change >= 0 ? "+" : "";
        return String.format("%s%.1f%%", sign, change);
    }

    private String calculatePercentageChange(int previous, int current) {
        return calculatePercentageChange((double) previous, (double) current);
    }

    // Quick actions
    public void navigateToNewDonation() {
        // This will be handled by the fragment
    }

    public void navigateToInventory() {
        // This will be handled by the fragment
    }

    public void navigateToTransparency() {
        // This will be handled by the fragment
    }

    public void navigateToReports() {
        // This will be handled by the fragment
    }

    // Utility methods
    public boolean hasRecentActivities() {
        List<Donation> activities = recentActivities.getValue();
        return activities != null && !activities.isEmpty();
    }

    public int getRecentActivitiesCount() {
        List<Donation> activities = recentActivities.getValue();
        return activities != null ? activities.size() : 0;
    }

    // Clear error message
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Data class for combined dashboard statistics
     */
    public static class DashboardStatistics {
        private final double totalDonations;
        private final int distributedItems;
        private final int familiesHelped;
        private final String donationsChange;
        private final String itemsChange;
        private final String familiesChange;

        public DashboardStatistics(double totalDonations, int distributedItems, int familiesHelped,
                                 String donationsChange, String itemsChange, String familiesChange) {
            this.totalDonations = totalDonations;
            this.distributedItems = distributedItems;
            this.familiesHelped = familiesHelped;
            this.donationsChange = donationsChange;
            this.itemsChange = itemsChange;
            this.familiesChange = familiesChange;
        }

        // Getters
        public double getTotalDonations() { return totalDonations; }
        public int getDistributedItems() { return distributedItems; }
        public int getFamiliesHelped() { return familiesHelped; }
        public String getDonationsChange() { return donationsChange; }
        public String getItemsChange() { return itemsChange; }
        public String getFamiliesChange() { return familiesChange; }

        public String getFormattedTotalDonations() {
            return String.format("₱%,.2f", totalDonations);
        }

        public String getFormattedDistributedItems() {
            return String.format("%,d", distributedItems);
        }

        public String getFormattedFamiliesHelped() {
            return String.format("%,d", familiesHelped);
        }
    }
}