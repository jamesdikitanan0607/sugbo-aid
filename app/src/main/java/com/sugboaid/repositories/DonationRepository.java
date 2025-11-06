package com.sugboaid.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.models.Donation;
import com.sugboaid.models.DonationType;
import com.sugboaid.models.Transaction;
import com.sugboaid.utils.SharedPreferencesHelper;
import com.sugboaid.utils.NetworkUtils;
import com.sugboaid.utils.OfflineQueueManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository class for managing donation data with LiveData for reactive updates
 */
public class DonationRepository {
    private SharedPreferencesHelper prefsHelper;
    private OfflineQueueManager offlineQueueManager;
    private Context context;
    private MutableLiveData<List<Donation>> donationsLiveData;
    private MutableLiveData<Double> totalDonationsLiveData;
    private MutableLiveData<Integer> totalFamiliesHelpedLiveData;
    private MutableLiveData<List<Donation>> recentDonationsLiveData;
    
    private static DonationRepository instance;

    // Private constructor for singleton pattern
    private DonationRepository(Context context) {
        this.context = context.getApplicationContext();
        prefsHelper = SharedPreferencesHelper.getInstance(context);
        offlineQueueManager = OfflineQueueManager.getInstance(context);
        donationsLiveData = new MutableLiveData<>();
        totalDonationsLiveData = new MutableLiveData<>();
        totalFamiliesHelpedLiveData = new MutableLiveData<>();
        recentDonationsLiveData = new MutableLiveData<>();
        
        // Initialize with existing data
        loadDonations();
    }

    // Singleton instance getter
    public static synchronized DonationRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DonationRepository(context.getApplicationContext());
        }
        return instance;
    }

    // LiveData getters
    public LiveData<List<Donation>> getDonations() {
        return donationsLiveData;
    }

    public LiveData<Double> getTotalDonations() {
        return totalDonationsLiveData;
    }

    public LiveData<Integer> getTotalFamiliesHelped() {
        return totalFamiliesHelpedLiveData;
    }

    public LiveData<List<Donation>> getRecentDonations() {
        return recentDonationsLiveData;
    }

    // Data operations
    public void addDonation(Donation donation) {
        try {
            if (donation == null) {
                throw new IllegalArgumentException("Donation cannot be null");
            }
            
            if (!donation.isValid()) {
                throw new IllegalArgumentException("Invalid donation data");
            }
            
            // Add to SharedPreferences (local storage)
            prefsHelper.addDonation(donation);
            
            // Create corresponding transaction
            Transaction transaction = Transaction.fromDonation(donation);
            prefsHelper.addTransaction(transaction);
            
            // Queue for sync if offline or add to sync queue for consistency
            if (!NetworkUtils.isNetworkAvailable(context)) {
                offlineQueueManager.queueDonationAction(OfflineQueueManager.ActionType.ADD_DONATION, donation);
            } else {
                // Even when online, queue the action for consistency and potential retry
                offlineQueueManager.queueDonationAction(OfflineQueueManager.ActionType.ADD_DONATION, donation);
            }
            
            // Refresh LiveData
            loadDonations();
            
        } catch (Exception e) {
            // Log error in production app
            throw new RuntimeException("Failed to add donation: " + e.getMessage(), e);
        }
    }

    public void updateDonation(Donation donation) {
        try {
            if (donation == null || !donation.isValid()) {
                throw new IllegalArgumentException("Invalid donation data");
            }
            
            List<Donation> donations = prefsHelper.getDonations();
            boolean updated = false;
            
            for (int i = 0; i < donations.size(); i++) {
                if (donations.get(i).getId().equals(donation.getId())) {
                    donations.set(i, donation);
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                prefsHelper.saveDonations(donations);
                
                // Queue for sync
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    offlineQueueManager.queueDonationAction(OfflineQueueManager.ActionType.UPDATE_DONATION, donation);
                } else {
                    offlineQueueManager.queueDonationAction(OfflineQueueManager.ActionType.UPDATE_DONATION, donation);
                }
                
                loadDonations();
            } else {
                throw new IllegalArgumentException("Donation not found for update");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update donation: " + e.getMessage(), e);
        }
    }

    public void deleteDonation(String donationId) {
        try {
            if (donationId == null || donationId.trim().isEmpty()) {
                throw new IllegalArgumentException("Donation ID cannot be null or empty");
            }
            
            // Get the donation before deleting for queue purposes
            Donation donationToDelete = getDonationById(donationId);
            if (donationToDelete != null) {
                // Queue for sync
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    offlineQueueManager.queueDonationAction(OfflineQueueManager.ActionType.DELETE_DONATION, donationToDelete);
                } else {
                    offlineQueueManager.queueDonationAction(OfflineQueueManager.ActionType.DELETE_DONATION, donationToDelete);
                }
            }
            
            prefsHelper.removeDonation(donationId);
            loadDonations();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete donation: " + e.getMessage(), e);
        }
    }

    public Donation getDonationById(String donationId) {
        if (donationId == null) return null;
        
        List<Donation> donations = prefsHelper.getDonations();
        for (Donation donation : donations) {
            if (donation.getId().equals(donationId)) {
                return donation;
            }
        }
        return null;
    }

    // Filter and search operations
    public LiveData<List<Donation>> getDonationsByType(DonationType type) {
        MutableLiveData<List<Donation>> filteredLiveData = new MutableLiveData<>();
        
        List<Donation> allDonations = prefsHelper.getDonations();
        List<Donation> filtered = new ArrayList<>();
        
        for (Donation donation : allDonations) {
            if (donation.getType() == type) {
                filtered.add(donation);
            }
        }
        
        filteredLiveData.setValue(filtered);
        return filteredLiveData;
    }

    public LiveData<List<Donation>> getDonationsByCampaign(String campaign) {
        MutableLiveData<List<Donation>> filteredLiveData = new MutableLiveData<>();
        
        List<Donation> allDonations = prefsHelper.getDonations();
        List<Donation> filtered = new ArrayList<>();
        
        for (Donation donation : allDonations) {
            if (campaign.equals(donation.getCampaign())) {
                filtered.add(donation);
            }
        }
        
        filteredLiveData.setValue(filtered);
        return filteredLiveData;
    }

    public LiveData<List<Donation>> searchDonations(String query) {
        MutableLiveData<List<Donation>> searchLiveData = new MutableLiveData<>();
        
        if (query == null || query.trim().isEmpty()) {
            searchLiveData.setValue(prefsHelper.getDonations());
            return searchLiveData;
        }
        
        List<Donation> allDonations = prefsHelper.getDonations();
        List<Donation> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        
        for (Donation donation : allDonations) {
            if (donation.getDonorName().toLowerCase().contains(lowerQuery) ||
                donation.getDescription().toLowerCase().contains(lowerQuery) ||
                donation.getCampaign().toLowerCase().contains(lowerQuery)) {
                filtered.add(donation);
            }
        }
        
        searchLiveData.setValue(filtered);
        return searchLiveData;
    }

    // Statistics and analytics
    public double calculateTotalCashDonations() {
        List<Donation> donations = prefsHelper.getDonations();
        double total = 0.0;
        
        for (Donation donation : donations) {
            if (donation.getType() == DonationType.CASH) {
                total += donation.getAmount();
            }
        }
        
        return total;
    }

    public int calculateTotalGoodsDonations() {
        List<Donation> donations = prefsHelper.getDonations();
        int total = 0;
        
        for (Donation donation : donations) {
            if (donation.getType() == DonationType.GOODS) {
                total += (int) donation.getAmount();
            }
        }
        
        return total;
    }

    public int calculateFamiliesHelped() {
        // Simplified calculation - in real app this would be more complex
        List<Donation> donations = prefsHelper.getDonations();
        double totalCash = calculateTotalCashDonations();
        int totalGoods = calculateTotalGoodsDonations();
        
        // Estimate: ₱1000 cash or 10 goods items helps 1 family
        int familiesFromCash = (int) (totalCash / 1000);
        int familiesFromGoods = totalGoods / 10;
        
        return familiesFromCash + familiesFromGoods;
    }

    public List<Donation> getTopDonations(int limit) {
        List<Donation> donations = new ArrayList<>(prefsHelper.getDonations());
        
        // Sort by amount (cash donations) and timestamp
        Collections.sort(donations, new Comparator<Donation>() {
            @Override
            public int compare(Donation d1, Donation d2) {
                if (d1.getType() == DonationType.CASH && d2.getType() == DonationType.CASH) {
                    return Double.compare(d2.getAmount(), d1.getAmount());
                } else {
                    return Long.compare(d2.getTimestamp(), d1.getTimestamp());
                }
            }
        });
        
        return donations.subList(0, Math.min(limit, donations.size()));
    }

    // Data refresh and synchronization
    public void refreshData() {
        loadDonations();
    }

    private void loadDonations() {
        try {
            List<Donation> donations = prefsHelper.getDonations();
            
            // Sort by timestamp (newest first)
            Collections.sort(donations, new Comparator<Donation>() {
                @Override
                public int compare(Donation d1, Donation d2) {
                    return Long.compare(d2.getTimestamp(), d1.getTimestamp());
                }
            });
            
            donationsLiveData.setValue(donations);
            
            // Update statistics
            double totalCash = calculateTotalCashDonations();
            totalDonationsLiveData.setValue(totalCash);
            
            int familiesHelped = calculateFamiliesHelped();
            totalFamiliesHelpedLiveData.setValue(familiesHelped);
            prefsHelper.saveTotalFamiliesHelped(familiesHelped);
            
            // Update recent donations (last 5)
            List<Donation> recent = donations.subList(0, Math.min(5, donations.size()));
            recentDonationsLiveData.setValue(recent);
            
        } catch (Exception e) {
            // In production, log this error
            donationsLiveData.setValue(new ArrayList<>());
            totalDonationsLiveData.setValue(0.0);
            totalFamiliesHelpedLiveData.setValue(0);
            recentDonationsLiveData.setValue(new ArrayList<>());
        }
    }

    // Data validation and cleanup
    public boolean validateAllDonations() {
        try {
            List<Donation> donations = prefsHelper.getDonations();
            for (Donation donation : donations) {
                if (!donation.isValid()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void cleanupInvalidDonations() {
        try {
            List<Donation> donations = prefsHelper.getDonations();
            List<Donation> validDonations = new ArrayList<>();
            
            for (Donation donation : donations) {
                if (donation.isValid()) {
                    validDonations.add(donation);
                }
            }
            
            if (validDonations.size() != donations.size()) {
                prefsHelper.saveDonations(validDonations);
                loadDonations();
            }
        } catch (Exception e) {
            // Log error in production
        }
    }

    // Clear all data
    public void clearAllDonations() {
        try {
            prefsHelper.clearDonations();
            loadDonations();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear donations: " + e.getMessage(), e);
        }
    }
}