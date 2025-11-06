package com.sugboaid.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sugboaid.models.AppNotification;
import com.sugboaid.models.Transaction;
import com.sugboaid.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository class for centralized settings and preferences management
 */
public class PreferencesRepository {
    private SharedPreferencesHelper prefsHelper;
    private MutableLiveData<Boolean> darkModeLiveData;
    private MutableLiveData<String> userRoleLiveData;
    private MutableLiveData<Boolean> offlineModeLiveData;
    private MutableLiveData<String> selectedCampaignLiveData;
    private MutableLiveData<List<AppNotification>> notificationsLiveData;
    private MutableLiveData<Integer> unreadNotificationCountLiveData;
    private MutableLiveData<List<Transaction>> transactionsLiveData;
    
    private static PreferencesRepository instance;

    // Private constructor for singleton pattern
    private PreferencesRepository(Context context) {
        prefsHelper = SharedPreferencesHelper.getInstance(context);
        darkModeLiveData = new MutableLiveData<>();
        userRoleLiveData = new MutableLiveData<>();
        offlineModeLiveData = new MutableLiveData<>();
        selectedCampaignLiveData = new MutableLiveData<>();
        notificationsLiveData = new MutableLiveData<>();
        unreadNotificationCountLiveData = new MutableLiveData<>();
        transactionsLiveData = new MutableLiveData<>();
        
        // Initialize with existing data
        loadPreferences();
        loadNotifications();
        loadTransactions();
    }

    // Singleton instance getter
    public static synchronized PreferencesRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesRepository(context.getApplicationContext());
        }
        return instance;
    }

    // LiveData getters for preferences
    public LiveData<Boolean> getDarkMode() {
        return darkModeLiveData;
    }

    public LiveData<String> getUserRole() {
        return userRoleLiveData;
    }

    public LiveData<Boolean> getOfflineMode() {
        return offlineModeLiveData;
    }

    public LiveData<String> getSelectedCampaign() {
        return selectedCampaignLiveData;
    }

    public LiveData<List<AppNotification>> getNotifications() {
        return notificationsLiveData;
    }

    public LiveData<Integer> getUnreadNotificationCount() {
        return unreadNotificationCountLiveData;
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactionsLiveData;
    }

    // Theme and UI preferences
    public void setDarkMode(boolean isDarkMode) {
        try {
            prefsHelper.saveDarkModePreference(isDarkMode);
            darkModeLiveData.setValue(isDarkMode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save dark mode preference: " + e.getMessage(), e);
        }
    }

    public boolean isDarkModeEnabled() {
        return prefsHelper.getDarkModePreference();
    }

    // User role management
    public void setUserRole(String role) {
        try {
            if (role == null || role.trim().isEmpty()) {
                throw new IllegalArgumentException("User role cannot be null or empty");
            }
            
            prefsHelper.saveUserRole(role);
            userRoleLiveData.setValue(role);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user role: " + e.getMessage(), e);
        }
    }

    public String getCurrentUserRole() {
        return prefsHelper.getUserRole();
    }

    // App state management
    public void setOfflineMode(boolean isOffline) {
        try {
            prefsHelper.saveOfflineMode(isOffline);
            offlineModeLiveData.setValue(isOffline);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save offline mode: " + e.getMessage(), e);
        }
    }

    public boolean isOfflineModeEnabled() {
        return prefsHelper.isOfflineMode();
    }

    public void setSelectedCampaign(String campaign) {
        try {
            if (campaign == null || campaign.trim().isEmpty()) {
                campaign = "General Relief";
            }
            
            prefsHelper.saveSelectedCampaign(campaign);
            selectedCampaignLiveData.setValue(campaign);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save selected campaign: " + e.getMessage(), e);
        }
    }

    public String getCurrentCampaign() {
        return prefsHelper.getSelectedCampaign();
    }

    // First launch and onboarding
    public boolean isFirstLaunch() {
        return prefsHelper.isFirstLaunch();
    }

    public void setFirstLaunchCompleted() {
        try {
            prefsHelper.saveFirstLaunch(false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save first launch status: " + e.getMessage(), e);
        }
    }

    // Sync and data management
    public long getLastSyncTime() {
        return prefsHelper.getLastSyncTime();
    }

    public void updateLastSyncTime() {
        try {
            prefsHelper.saveLastSyncTime(System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update sync time: " + e.getMessage(), e);
        }
    }

    // Notification management
    public void addNotification(AppNotification notification) {
        try {
            if (notification == null || !notification.isValid()) {
                throw new IllegalArgumentException("Invalid notification data");
            }
            
            prefsHelper.addNotification(notification);
            loadNotifications();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add notification: " + e.getMessage(), e);
        }
    }

    public void markNotificationAsRead(int notificationId) {
        try {
            prefsHelper.markNotificationAsRead(notificationId);
            loadNotifications();
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark notification as read: " + e.getMessage(), e);
        }
    }

    public void markAllNotificationsAsRead() {
        try {
            prefsHelper.markAllNotificationsAsRead();
            loadNotifications();
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark all notifications as read: " + e.getMessage(), e);
        }
    }

    public void deleteNotification(int notificationId) {
        try {
            List<AppNotification> notifications = prefsHelper.getNotifications();
            notifications.removeIf(notification -> notification.getId() == notificationId);
            prefsHelper.saveNotifications(notifications);
            loadNotifications();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete notification: " + e.getMessage(), e);
        }
    }

    public void clearAllNotifications() {
        try {
            prefsHelper.clearNotifications();
            loadNotifications();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear notifications: " + e.getMessage(), e);
        }
    }

    // Transaction management
    public void addTransaction(Transaction transaction) {
        try {
            if (transaction == null || !transaction.isValid()) {
                throw new IllegalArgumentException("Invalid transaction data");
            }
            
            prefsHelper.addTransaction(transaction);
            loadTransactions();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add transaction: " + e.getMessage(), e);
        }
    }

    public void deleteTransaction(String transactionId) {
        try {
            if (transactionId == null || transactionId.trim().isEmpty()) {
                throw new IllegalArgumentException("Transaction ID cannot be null or empty");
            }
            
            prefsHelper.removeTransaction(transactionId);
            loadTransactions();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete transaction: " + e.getMessage(), e);
        }
    }

    public void clearAllTransactions() {
        try {
            prefsHelper.clearTransactions();
            loadTransactions();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear transactions: " + e.getMessage(), e);
        }
    }

    // Data export and backup
    public String exportAllData() {
        try {
            return prefsHelper.exportAllData();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export data: " + e.getMessage(), e);
        }
    }

    public boolean importData(String jsonData) {
        try {
            boolean success = prefsHelper.importData(jsonData);
            if (success) {
                refreshAllData();
            }
            return success;
        } catch (Exception e) {
            throw new RuntimeException("Failed to import data: " + e.getMessage(), e);
        }
    }

    // Data validation and integrity
    public boolean validateDataIntegrity() {
        try {
            return prefsHelper.validateDataIntegrity();
        } catch (Exception e) {
            return false;
        }
    }

    public void clearAllData() {
        try {
            prefsHelper.clearAllData();
            refreshAllData();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear all data: " + e.getMessage(), e);
        }
    }

    // Utility methods for common operations
    public List<String> getAvailableCampaigns() {
        // In a real app, this might come from a server or be configurable
        List<String> campaigns = new ArrayList<>();
        campaigns.add("General Relief");
        campaigns.add("Typhoon Response");
        campaigns.add("Flood Relief");
        campaigns.add("Emergency Aid");
        campaigns.add("Community Support");
        return campaigns;
    }

    public List<String> getAvailableRoles() {
        List<String> roles = new ArrayList<>();
        roles.add("Donor");
        roles.add("Organization");
        roles.add("Volunteer");
        roles.add("Recipient");
        roles.add("Guest");
        return roles;
    }

    // Statistics and summary methods
    public int getTotalNotificationCount() {
        return prefsHelper.getNotifications().size();
    }

    public int getUnreadNotificationCountValue() {
        return prefsHelper.getUnreadNotificationCount();
    }

    public int getTotalTransactionCount() {
        return prefsHelper.getTransactions().size();
    }

    // Data refresh methods
    public void refreshAllData() {
        loadPreferences();
        loadNotifications();
        loadTransactions();
    }

    private void loadPreferences() {
        try {
            darkModeLiveData.setValue(prefsHelper.getDarkModePreference());
            userRoleLiveData.setValue(prefsHelper.getUserRole());
            offlineModeLiveData.setValue(prefsHelper.isOfflineMode());
            selectedCampaignLiveData.setValue(prefsHelper.getSelectedCampaign());
        } catch (Exception e) {
            // Set default values on error
            darkModeLiveData.setValue(false);
            userRoleLiveData.setValue("Guest");
            offlineModeLiveData.setValue(false);
            selectedCampaignLiveData.setValue("General Relief");
        }
    }

    private void loadNotifications() {
        try {
            List<AppNotification> notifications = prefsHelper.getNotifications();
            
            // Sort by timestamp (newest first)
            Collections.sort(notifications, new Comparator<AppNotification>() {
                @Override
                public int compare(AppNotification n1, AppNotification n2) {
                    return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                }
            });
            
            notificationsLiveData.setValue(notifications);
            unreadNotificationCountLiveData.setValue(prefsHelper.getUnreadNotificationCount());
            
        } catch (Exception e) {
            notificationsLiveData.setValue(new ArrayList<>());
            unreadNotificationCountLiveData.setValue(0);
        }
    }

    private void loadTransactions() {
        try {
            List<Transaction> transactions = prefsHelper.getTransactions();
            
            // Sort by date (newest first)
            Collections.sort(transactions, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    return Long.compare(t2.getTimestamp(), t1.getTimestamp());
                }
            });
            
            transactionsLiveData.setValue(transactions);
            
        } catch (Exception e) {
            transactionsLiveData.setValue(new ArrayList<>());
        }
    }

    // Statistics tracking for percentage calculations
    public double getPreviousTotalDonations() {
        return prefsHelper.getPreviousTotalDonations();
    }

    public void savePreviousTotalDonations(double amount) {
        prefsHelper.savePreviousTotalDonations(amount);
    }

    public int getPreviousDistributedItems() {
        return prefsHelper.getPreviousDistributedItems();
    }

    public void savePreviousDistributedItems(int items) {
        prefsHelper.savePreviousDistributedItems(items);
    }

    public int getPreviousFamiliesHelped() {
        return prefsHelper.getPreviousFamiliesHelped();
    }

    public void savePreviousFamiliesHelped(int families) {
        prefsHelper.savePreviousFamiliesHelped(families);
    }

    // Helper method to create default notifications for testing
    public void createSampleNotifications() {
        try {
            List<AppNotification> notifications = new ArrayList<>();
            
            notifications.add(AppNotification.createDonationNotification("John Doe", "₱1,000"));
            notifications.add(AppNotification.createLowInventoryNotification("Rice", 15));
            notifications.add(AppNotification.createDistributionNotification("Barangay Lahug", 25));
            
            for (AppNotification notification : notifications) {
                prefsHelper.addNotification(notification);
            }
            
            loadNotifications();
        } catch (Exception e) {
            // Log error in production
        }
    }
}